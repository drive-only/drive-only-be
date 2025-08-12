package drive_only.drive_only_server.service.member;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.LikedCourse;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.course.list.MyCourseListResponse;
import drive_only.drive_only_server.dto.course.search.MyCourseSearchResponse;
import drive_only.drive_only_server.dto.likedCourse.list.LikedCourseListResponse;
import drive_only.drive_only_server.dto.likedCourse.search.LikedCourseSearchResponse;
import drive_only.drive_only_server.dto.member.MemberUpdateRequest;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.exception.custom.MemberNotFoundException;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.repository.course.LikedCourseRepository;
import drive_only.drive_only_server.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
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
    private final LikedCourseRepository likedCourseRepository;
    private final CourseRepository courseRepository;

    // -----------------------
    // 생성/로그인
    // -----------------------
    @Transactional
    public Member registerOrLogin(OAuthUserInfo userInfo) {
        String email = normalizeEmail(userInfo.getEmail());
        ProviderType provider = userInfo.getProvider();

        // 정규화된 키로 조회
        Optional<Member> found = memberRepository.findByEmailAndProvider(email, provider);
        if (found.isPresent()) {
            return found.get();
        }

        // 없으면 생성 (엔티티 내부에서 검증/정규화 재확인)
        Member newMember = Member.createMember(
                email,
                normalizeNullable(userInfo.getNickname()),
                normalizeNullable(userInfo.getProfileImageUrl()),
                provider
        );
        return memberRepository.save(newMember);
    }

    // -----------------------
    // 조회 계열
    // -----------------------
    public Member findByEmailAndProvider(String email, ProviderType provider) {
        return memberRepository.findByEmailAndProvider(normalizeEmail(email), provider)
                .orElseThrow(MemberNotFoundException::new);
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(MemberNotFoundException::new);
    }

    // -----------------------
    // 수정/삭제
    // -----------------------
    @Transactional
    public Member updateMember(String email, ProviderType provider, MemberUpdateRequest request) {
        Member member = memberRepository.findByEmailAndProvider(normalizeEmail(email), provider)
                .orElseThrow(MemberNotFoundException::new);

        if (request.getNickname() != null) {
            // 엔티티에서 규칙 검증 및 trim
            member.updateNickname(request.getNickname());
        }
        if (request.getProfileImageUrl() != null) {
            member.updateProfileImageUrl(request.getProfileImageUrl());
        }
        // JPA 더티 체킹
        return member;
    }

    @Transactional
    public void deleteMemberByEmailAndProvider(String email, ProviderType provider) {
        Member member = memberRepository.findByEmailAndProvider(normalizeEmail(email), provider)
                .orElseThrow(MemberNotFoundException::new);
        memberRepository.delete(member);
    }

    // -----------------------
    // 좋아요/내 코스 커서 조회
    // -----------------------
    public LikedCourseListResponse getLikedCourses(Member member, Long lastId, int size) {
        List<LikedCourse> likedCourses = likedCourseRepository.findLikedCoursesByMember(member, lastId, size);

        List<LikedCourseSearchResponse> responses = likedCourses.stream()
                .map(lc -> LikedCourseSearchResponse.from(lc.getCourse()))
                .toList();

        Long newLastId = responses.isEmpty() ? null : responses.get(responses.size() - 1).courseId();
        boolean hasNext = likedCourses.size() == size;

        return LikedCourseListResponse.from(responses, newLastId, size, hasNext);
    }

    public MyCourseListResponse getMyCourses(Member member, Long lastId, int size) {
        List<Course> courses = courseRepository.findCoursesByMember(member.getId(), lastId, size);

        List<MyCourseSearchResponse> responseList = courses.stream()
                .map(MyCourseSearchResponse::from)
                .toList();

        Long newLastId = responseList.isEmpty() ? null : responseList.get(responseList.size() - 1).courseId();
        boolean hasNext = courses.size() == size;

        return MyCourseListResponse.from(responseList, newLastId, size, hasNext);
    }

    // -----------------------
    // 유틸(정규화)
    // -----------------------
    private static String normalizeEmail(String e) {
        return e == null ? null : e.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeNullable(String s) {
        return s == null ? null : s.trim();
    }
}