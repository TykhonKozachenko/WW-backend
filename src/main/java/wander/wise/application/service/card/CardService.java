package wander.wise.application.service.card;

import java.util.List;
import org.springframework.data.domain.Pageable;
import wander.wise.application.dto.card.*;

public interface CardService {
    CardWithoutDistanceDto createNewCard(String email, CreateCardRequestDto requestDto);

    CardWithoutDistanceDto findById(Long id);

    CardWithoutDistanceDto updateById(Long id, String email, CreateCardRequestDto requestDto);

    List<CardDto> search(Pageable pageable, CardSearchParameters searchParameters);

    void postLike(Long id, String email);

    void removeLike(Long id, String email);

    void hideCard(Long id);

    void revealCard(Long id);

    void deleteById(Long id, String email);

    void report(Long id, String email, ReportCardRequestDto requestDto);

    CardWithoutDistanceDto findByIdAsAdmin(Long id);

    void addCardToSaved(Long id, String email);

    void removeCardFromSaved(Long id, String email);
}
