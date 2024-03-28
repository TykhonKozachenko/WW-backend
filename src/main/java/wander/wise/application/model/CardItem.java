package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
@Table(name = "card_items")
public class CardItem {
    @Id
    private Long id;
    @OneToOne
    private Card card;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(name = "why_this_place", nullable = false)
    private String whyThisPlace;
    @Column(nullable = false)
    private String cost;
    //imageLinks = "link|link|link|link"
    @Column(name = "image_links", nullable = false)
    private String imageLinks;
    @Column(name = "map_link", nullable = false)
    private String mapLink;
    @Column(nullable = false)
    private BigDecimal latitude;
    @Column(nullable = false)
    private BigDecimal longitude;
}
