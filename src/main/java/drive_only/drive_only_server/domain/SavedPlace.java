package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "saved_place")
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
        validateExistingPlace(member, place);
        setMember(member);
        setPlace(place);
    }

    public boolean isSamePlace(Long placeId) {
        return this.place.getId().equals(placeId);
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

    private void validateExistingPlace(Member member, Place place) {
        boolean isAlreadyExistedPlace = member.getSavedPlaces().stream()
                .anyMatch(sp -> sp.isSamePlace(place.getId()));
        if (isAlreadyExistedPlace) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTED_PLACE);
        }
    }
}
