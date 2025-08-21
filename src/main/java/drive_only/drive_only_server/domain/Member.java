package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"email", "provider"}))
public class Member {
    private static final int EMAIL_MAX = 320;
    private static final int NICK_MIN = 2;
    private static final int NICK_MAX = 20;
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern NICK_REGEX =
            Pattern.compile("^[a-zA-Z0-9가-힣_.-]+$");

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private ProviderType provider;

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SavedPlace> savedPlaces = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<LikedComment> likedComments = new ArrayList<>();

    public static Member createMember(String email, String nickname, String profileImageUrl, ProviderType provider) {
        validateEmail(email);
        validateProvider(provider);
        if (nickname != null) validateNickname(nickname);

        Member m = new Member();
        m.email = normalizeEmail(email);
        m.nickname = normalizeNullable(nickname);
        m.profileImageUrl = normalizeNullable(profileImageUrl);
        m.provider = provider;
        return m;
    }

    public void updateNickname(String nickname) {
        if (nickname != null) validateNickname(nickname);
        this.nickname = normalizeNullable(nickname);
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = normalizeNullable(profileImageUrl);
    }

    public void addLikedComment(LikedComment likedComment) {
        this.getLikedComments().add(likedComment);
        likedComment.setMember(this);
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()
                || email.length() > EMAIL_MAX
                || !EMAIL_REGEX.matcher(email).matches()) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL);
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname.isBlank()
                || nickname.length() < NICK_MIN || nickname.length() > NICK_MAX
                || !NICK_REGEX.matcher(nickname).matches()) {
            throw new BusinessException(ErrorCode.INVALID_NICKNAME);
        }
    }

    private static void validateProvider(ProviderType provider) {
        if (provider == null) {
            throw new BusinessException(ErrorCode.INVALID_PROVIDER);
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
    private static String normalizeNullable(String s) {
        return (s == null) ? null : s.trim();
    }
}
