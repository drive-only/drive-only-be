package drive_only.drive_only_server.dto.member;

public record MyProfileResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        drive_only.drive_only_server.domain.ProviderType provider
) {}
