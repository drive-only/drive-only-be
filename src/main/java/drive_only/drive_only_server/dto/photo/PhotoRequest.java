package drive_only.drive_only_server.dto.photo;

public record PhotoRequest(
        String s3Key,
        String photoUrl
) {}
