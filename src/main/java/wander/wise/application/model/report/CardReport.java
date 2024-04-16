package wander.wise.application.model.report;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import wander.wise.application.model.Card;

@Entity
@Table(name = "card_reports")
@Setter
@Getter
@RequiredArgsConstructor
public class CardReport extends Report {
    @ManyToOne
    private Card card;
}
