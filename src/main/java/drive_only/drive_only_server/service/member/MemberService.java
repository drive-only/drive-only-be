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
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.repository.course.LikedCourseRepository;
import drive_only.drive_only_server.repository.member.MemberRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final LikedCourseRepository likedCourseRepository;
    private final CourseRepository courseRepository;

    private static String normalizeEmail(String e) {
        return e == null ? null : e.trim().toLowerCase(java.util.Locale.ROOT);
    }
    private static String normalizeNullable(String s) {
        return s == null ? null : s.trim();
    }

    @Transactional
    public Member registerOrLogin(OAuthUserInfo userInfo) {
        String email = normalizeEmail(userInfo.getEmail());
        ProviderType provider = userInfo.getProvider();

        return memberRepository.findByEmailAndProvider(email, provider)
                .orElseGet(() -> {
                    // createMember 내부에서도 정규화/검증하지만, 조회 키 일치 위해 find 전에 정규화 필수
                    Member newMember = Member.createMember(
                            email,
                            normalizeNullable(userInfo.getNickname()),
                            normalizeNullable(userInfo.getProfileImageUrl()),
                            provider
                    );
                    return memberRepository.save(newMember);
                });
    }

    public Member findByEmailAndProvider(String email, ProviderType provider) {
        return memberRepository.findByEmailAndProvider(normalizeEmail(email), provider)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public Member updateMember(String email, ProviderType provider, MemberUpdateRequest request) {
        Member member = memberRepository.findByEmailAndProvider(normalizeEmail(email), provider)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (request.getNickname() != null) {
            member.updateNickname(request.getNickname()); // 엔티티에서 검증
        }
        if (request.getProfileImageUrl() != null) {
            member.updateProfileImageUrl(request.getProfileImageUrl());
        }
        return member; // JPA 더티체킹으로 저장
    }

    @Transactional
    public void deleteMemberByEmailAndProvider(String email, ProviderType provider) {
        Member member = memberRepository.findByEmailAndProvider(normalizeEmail(email), provider)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        memberRepository.delete(member);
    }

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
}