package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import wander.wise.application.model.report.CardReport;

import java.util.HashSet;
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
    @ManyToOne()
    private FilterSet filterSet;
    @OneToOne (mappedBy = "card", cascade = CascadeType.ALL)
    private CardItem cardItem;
    private Long likes;
    @OneToMany(mappedBy = "card")
    private Set<Comment> comments = new HashSet<>();
    @OneToMany(mappedBy = "card")
    private Set<CardReport> reports = new HashSet<>();
    private boolean shown = false;
}
