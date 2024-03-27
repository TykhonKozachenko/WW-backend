package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import wander.wise.application.model.report.CardReport;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "cards")
@Setter
@Getter
@RequiredArgsConstructor
@SoftDelete
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String author;
    @ManyToOne
    private FilterSet filterSet;
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
    @Column(nullable = false)
    private Long likes;
    @OneToMany(mappedBy = "card")
    private Set<Comment> comments;
    @OneToMany(mappedBy = "card")
    private Set<CardReport> reports;
    private boolean shown = false;
}
