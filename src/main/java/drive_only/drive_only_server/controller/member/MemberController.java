package drive_only.drive_only_server.controller.member;

import drive_only.drive_only_server.dto.common.ApiResult;
import drive_only.drive_only_server.dto.common.ApiResultSupport;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.place.myPlace.DeleteSavedPlaceResponse;
import drive_only.drive_only_server.dto.place.myPlace.SavePlaceResponse;
import drive_only.drive_only_server.dto.place.search.SavedPlaceSearchResponse;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.course.list.MyCourseListResponse;
import drive_only.drive_only_server.dto.likedCourse.list.LikedCourseListResponse;
import drive_only.drive_only_server.dto.member.MemberResponse;
import drive_only.drive_only_server.dto.member.MemberUpdateRequest;
import drive_only.drive_only_server.dto.member.OtherMemberResponse;
import drive_only.drive_only_server.security.CustomUserPrincipal;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.security.LoginMemberProvider;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import drive_only.drive_only_server.service.member.MemberService;
import drive_only.drive_only_server.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 API")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "마이페이지", description = "마이페이지 조회")
    @ApiErrorCodeExamples({
            // 401 Access
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/members/me")
    public ResponseEntity<ApiResult<PaginatedResponse<MemberResponse>>> getMyProfile() {
        PaginatedResponse<MemberResponse> result = memberService.getMyProfile();
        return ApiResultSupport.ok(SuccessCode.SUCCESS_GET_MY_PROFILE, result);
    }

    @Operation(summary = "다른 회원 정보 조회", description = "회원 ID로 다른 회원 정보 조회")
    @ApiErrorCodeExamples({
            ErrorCode.MEMBER_NOT_FOUND,

            // 401 Access
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/members/{id}")
    public ResponseEntity<ApiResult<PaginatedResponse<OtherMemberResponse>>> getOtherMemberProfile(@PathVariable Long id) {
        PaginatedResponse<OtherMemberResponse> result = memberService.findOtherMember(id);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_GET_OTHER_MEMBER, result);
    }

    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 닉네임, 프로필 이미지를 수정합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_NICKNAME,
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PatchMapping("/api/members/me")
    public ResponseEntity<ApiResult<PaginatedResponse<MemberResponse>>> updateMyProfile(
            @RequestBody MemberUpdateRequest request
    ) {
        PaginatedResponse<MemberResponse> result = memberService.updateMember(request);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_UPDATE_MEMBER, result);
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원 탈퇴")
    @ApiErrorCodeExamples({
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/api/members/me")
    public ResponseEntity<ApiResult<Void>> deleteMyAccount(
            Authentication authentication,
            @CookieValue(value = "refresh-token", required = false) String refreshToken
    ) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String email = principal.getEmail();
        ProviderType provider = principal.getProvider();

        // 1. 회원 삭제
        memberService.deleteMemberByEmailAndProvider(email, provider);

        // 2. refresh token 삭제
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            refreshTokenService.deleteRefreshToken(email);
        }

        // 3. access-token 및 refresh-token 쿠키 삭제 설정
        ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refresh-token", "")
                .httpOnly(true)
                .secure(true)
//                .domain("api.drive-only.com")
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        ResponseCookie deleteAccessTokenCookie = ResponseCookie.from("access-token", "")
                .httpOnly(true)
                .secure(true)
//                .domain("api.drive-only.com")
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        // 4. 다중 쿠키 설정을 위한 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, deleteAccessTokenCookie.toString());

        // 5. 응답 반환
        return ApiResultSupport.okWithCookies(
                SuccessCode.SUCCESS_DELETE_MEMBER,
                null,
                deleteRefreshTokenCookie, deleteAccessTokenCookie
        );
    }

    @Operation(summary = "좋아요한 코스 조회", description = "회원이 좋아요한 드라이브 코스를 최신순으로 조회")
    @ApiErrorCodeExamples({
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/members/me/likedCourses")
    public ResponseEntity<ApiResult<LikedCourseListResponse>> getLikedCourses(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size
    ) {
        LikedCourseListResponse result = memberService.getLikedCourses(lastId, size);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_GET_LIKED_COURSES, result);
    }

    @Operation(summary = "작성한 코스 조회", description = "인증된 사용자의 작성 코스를 커서 기반으로 조회")
    @ApiErrorCodeExamples({
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/members/me/courses")
    public ResponseEntity<ApiResult<MyCourseListResponse>> getMyCourses(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size
    ) {
        MyCourseListResponse result = memberService.getMyCourses(lastId, size);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_GET_MY_COURSES, result);
    }

    @Operation(summary = "저장한 장소 리스트 조회", description = "사용자가 저장했던 장소들을 조회")
    @ApiErrorCodeExamples({
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/members/me/saved-places")
    public ResponseEntity<ApiResult<PaginatedResponse<SavedPlaceSearchResponse>>> getSavedPlaces() {
        PaginatedResponse<SavedPlaceSearchResponse> result = memberService.searchSavedPlaces();
        return ApiResultSupport.ok(SuccessCode.SUCCESS_GET_SAVED_PLACES, result);
    }

    @Operation(summary = "장소 저장", description = "사용자가 저장하고 싶은 장소 등록")
    @ApiErrorCodeExamples({
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/members/me/{placeId}")
    public ResponseEntity<ApiResult<SavePlaceResponse>> savePlace(@PathVariable Long placeId) {
        SavePlaceResponse result = memberService.savePlace(placeId);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_SAVE_PLACE, result);
    }

    @Operation(summary = "저장한 장소 삭제", description = "사용자가 저장했던 장소를 삭제")
    @ApiErrorCodeExamples({
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.SAVED_PLACE_NOT_FOUND,
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/api/members/me/saved-places/{savedPlaceId}")
    public ResponseEntity<ApiResult<DeleteSavedPlaceResponse>> deleteSavedPlace(@PathVariable Long savedPlaceId) {
        DeleteSavedPlaceResponse result = memberService.deleteSavedPlace(savedPlaceId);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_DELETE_SAVED_PLACE, result);
    }
}
