package drive_only.drive_only_server.dto.tag;

import lombok.Getter;

@Getter
public class TagDto {
    private Long tagId;
    private String tagName;

    public TagDto(Long tagId, String tagName) {
        this.tagId = tagId;
        this.tagName = tagName;
    }
}
