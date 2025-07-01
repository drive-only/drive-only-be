package drive_only.drive_only_server.dto.tag;

import lombok.Getter;

@Getter
public class TagRequest {
    private String tagName;

    public TagRequest(String tagName) {
        this.tagName = tagName;
    }
}
