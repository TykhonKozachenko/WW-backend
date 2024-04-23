package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "collections")
@Setter
@Getter
@RequiredArgsConstructor
@SoftDelete
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private String name;
    @Column(name = "image_link")
    private String imageLink;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "collection_card",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "card_id"))
    Set<Card> cards = new HashSet<>();
    @Column(name = "is_public")
    boolean isPublic = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Collection that)) return false;
        return isPublic == that.isPublic
                && Objects.equals(id, that.id)
                && Objects.equals(user, that.user)
                && Objects.equals(name, that.name)
                && Objects.equals(imageLink, that.imageLink)
                && Objects.equals(cards, that.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                user,
                name,
                imageLink,
                cards,
                isPublic);
    }
}
