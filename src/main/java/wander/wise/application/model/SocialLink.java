package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "social_links")
@Setter
@Getter
@RequiredArgsConstructor
@SoftDelete
public class SocialLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private String name;
    private String link;
}
