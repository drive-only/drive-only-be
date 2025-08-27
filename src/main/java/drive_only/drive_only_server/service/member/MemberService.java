package drive_only.drive_only_server.service.member;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.LikedCourse;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.domain.SavedPlace;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.course.list.MyCourseListResponse;
import drive_only.drive_only_server.dto.course.search.MyCourseSearchResponse;
import drive_only.drive_only_server.dto.likedCourse.list.LikedCourseListResponse;
import drive_only.drive_only_server.dto.likedCourse.search.LikedCourseSearchResponse;
import drive_only.drive_only_server.dto.member.MemberResponse;
import drive_only.drive_only_server.dto.member.MemberUpdateRequest;
import drive_only.drive_only_server.dto.member.OtherMemberResponse;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.dto.place.myPlace.DeleteSavedPlaceResponse;
import drive_only.drive_only_server.dto.place.myPlace.SavePlaceResponse;
import drive_only.drive_only_server.dto.place.search.SavedPlaceSearchResponse;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.custom.MemberNotFoundException;
import drive_only.drive_only_server.exception.custom.PlaceNotFoundException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.repository.course.LikedCourseRepository;
import drive_only.drive_only_server.repository.course.SavedPlaceRepository;
import drive_only.drive_only_server.repository.member.MemberRepository;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import drive_only.drive_only_server.security.LoginMemberProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final LoginMemberProvider loginMemberProvider;
    private final LikedCourseRepository likedCourseRepository;
    private final CourseRepository courseRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final PlaceRepository placeRepository;

    public PaginatedResponse<MemberResponse> getMyProfile() {
        Member member = loginMemberProvider.getLoginMember();
        MemberResponse response = new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getProvider()
        );
        return new PaginatedResponse<>(List.of(response), null);
    }

    public PaginatedResponse<OtherMemberResponse> findOtherMember(Long id) {
        Member member = findById(id);
        OtherMemberResponse response = new OtherMemberResponse(
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl()
        );
        return new PaginatedResponse<>(List.of(response), null);
    }

    @Transactional
    public PaginatedResponse<MemberResponse> updateMember(MemberUpdateRequest request) {
        String email = loginMemberProvider.getLoginMember().getEmail();
        ProviderType provider = loginMemberProvider.getLoginMember().getProvider();
        Member member = memberRepository.findByEmailAndProvider(normalizeEmail(email), provider)
                .orElseThrow(MemberNotFoundException::new);

        if (request.getNickname() != null) {
            member.updateNickname(request.getNickname());
        }
        if (request.getProfileImageUrl() != null) {
            member.updateProfileImageUrl(request.getProfileImageUrl());
        }

        MemberResponse response = new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getProvider()
        );
        return new PaginatedResponse<>(List.of(response), null);
    }

    @Transactional
    public Member registerOrLogin(OAuthUserInfo userInfo) {
        String email = normalizeEmail(userInfo.getEmail());
        ProviderType provider = userInfo.getProvider();

        Optional<Member> found = memberRepository.findByEmailAndProvider(email, provider);
        if (found.isPresent()) {
            return found.get();
        }

        Member newMember = Member.createMember(
                email,
                normalizeNullable(userInfo.getNickname()),
                normalizeNullable(userInfo.getProfileImageUrl()),
                provider
        );
        return memberRepository.save(newMember);
    }

    @Transactional
    public void deleteMemberByEmailAndProvider(String email, ProviderType provider) {
        Member member = memberRepository.findByEmailAndProvider(normalizeEmail(email), provider)
                .orElseThrow(MemberNotFoundException::new);
        memberRepository.delete(member);
    }

    public LikedCourseListResponse getLikedCourses(Long lastId, int size) {
        Member member = loginMemberProvider.getLoginMember();
        List<LikedCourse> likedCourses = likedCourseRepository.findLikedCoursesByMember(member, lastId, size);

        List<LikedCourseSearchResponse> responses = likedCourses.stream()
                .map(lc -> LikedCourseSearchResponse.from(lc.getCourse()))
                .toList();

        Long newLastId = responses.isEmpty() ? null : responses.get(responses.size() - 1).courseId();
        boolean hasNext = likedCourses.size() == size;

        return LikedCourseListResponse.from(responses, newLastId, size, hasNext);
    }

    public MyCourseListResponse getMyCourses(Long lastId, int size) {
        Member member = loginMemberProvider.getLoginMember();
        List<Course> courses = courseRepository.findCoursesByMember(member.getId(), lastId, size);

        List<MyCourseSearchResponse> responseList = courses.stream()
                .map(MyCourseSearchResponse::from)
                .toList();

        Long newLastId = responseList.isEmpty() ? null : responseList.get(responseList.size() - 1).courseId();
        boolean hasNext = courses.size() == size;

        return MyCourseListResponse.from(responseList, newLastId, size, hasNext);
    }

    public PaginatedResponse<SavedPlaceSearchResponse> searchSavedPlaces() {
        Member member = loginMemberProvider.getLoginMember();
        List<SavedPlace> savedPlaces = savedPlaceRepository.findByMember(member);
        List<SavedPlaceSearchResponse> results = savedPlaces.stream()
                .map(savedPlace -> {
                    Place place = savedPlace.getPlace();
                    return SavedPlaceSearchResponse.from(place, savedPlace.getId());
                })
                .toList();
        return new PaginatedResponse<>(results, null);
    }

    @Transactional
    public SavePlaceResponse savePlace(Long placeId) {
        Member member = loginMemberProvider.getLoginMember();
        Place place = findPlaceById(placeId);
        SavedPlace savedPlace = savedPlaceRepository.save(new SavedPlace(member, place));
        return new SavePlaceResponse(savedPlace.getId());
    }

    @Transactional
    public DeleteSavedPlaceResponse deleteSavedPlace(Long savedPlaceId) {
        Member member = loginMemberProvider.getLoginMember();
        SavedPlace savedPlace = member.getSavedPlaces().stream()
                .filter(sp -> sp.getId().equals(savedPlaceId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SAVED_PLACE_NOT_FOUND));
        savedPlaceRepository.delete(savedPlace);
        return new DeleteSavedPlaceResponse(savedPlace.getId());
    }

    private Place findPlaceById(Long placeId) {
        return placeRepository.findById(placeId).orElseThrow(PlaceNotFoundException::new);
    }

    private Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);
    }

    private static String normalizeEmail(String e) {
        return e == null ? null : e.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeNullable(String s) {
        return s == null ? null : s.trim();
    }
}
