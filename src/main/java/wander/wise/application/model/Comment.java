package wander.wise.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import wander.wise.application.model.report.CommentReport;

@Entity
@Table(name = "comments")
@Setter
@Getter
@RequiredArgsConstructor
@SoftDelete
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Card card;
    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime timeStamp;
    @Column(nullable = false)
    private String text;
    private Integer stars = 5;
    @OneToMany(mappedBy = "comment")
    private Set<CommentReport> reports;
    private boolean shown = true;
}
