package drive_only.drive_only_server.dto.coursePlace.create;

import drive_only.drive_only_server.dto.photo.PhotoRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CoursePlaceCreateRequest(
        String placeId,
        String placeType,
        String placeName,
        String placeAddress,

        @Size(min = 1, max = 500, message = "내용은 1자 이상 500자 이하로 입력해주세요.")
        @NotBlank(message = "내용을 입력해주세요.")
        String content,

        @Size(max = 5, message = "사진은 최대 5개까지 첨부할 수 있습니다.")
        List<PhotoRequest> photoUrls,
        int sequence
) {}
