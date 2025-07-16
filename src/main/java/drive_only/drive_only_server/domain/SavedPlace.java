package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    public SavedPlace(Member member, Place place) {
        setMember(member);
        setPlace(place);
    }

    private void setMember(Member member) {
        this.member = member;
        if (!member.getSavedPlaces().contains(this)) {
            member.getSavedPlaces().add(this);
        }
    }

    private void setPlace(Place place) {
        this.place = place;
    }
}
