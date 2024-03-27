package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import wander.wise.application.model.report.CommentReport;
import java.time.LocalDateTime;
import java.util.Set;

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
    @OneToMany(mappedBy = "comment")
    private Set<CommentReport> reports;
    private boolean shown = true;
}

