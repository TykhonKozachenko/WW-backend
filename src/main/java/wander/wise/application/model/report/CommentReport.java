package wander.wise.application.model.report;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import wander.wise.application.model.Comment;

@Entity
@Table(name = "comment_reports")
@Setter
@Getter
@RequiredArgsConstructor
public class CommentReport extends Report {
    @ManyToOne
    private Comment comment;
}
