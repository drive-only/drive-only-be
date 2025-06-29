package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "provider")
    private String provider;

    public Member(String email, String nickname, String profileImageUrl, String provider) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
    }
}
