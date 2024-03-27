package wander.wise.application.model.report;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import wander.wise.application.model.User;
import java.time.LocalDateTime;

@MappedSuperclass
@Setter
@Getter
@RequiredArgsConstructor
@SoftDelete
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime timeStamp;
    @Column(nullable = false)
    private String text;
}
