package wander.wise.application.service.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import wander.wise.application.dto.ai.AiResponseDto;
import wander.wise.application.dto.card.CardDto;
import wander.wise.application.dto.card.CardSearchParameters;
import wander.wise.application.dto.card.CardWithoutDistanceDto;
import wander.wise.application.dto.card.CreateCardRequestDto;
import wander.wise.application.dto.maps.MapsResponseDto;
import wander.wise.application.exception.CardSearchException;
import wander.wise.application.mapper.CardMapper;
import wander.wise.application.model.Card;
import wander.wise.application.repository.card.CardRepository;
import wander.wise.application.repository.card.CardSpecificationBuilder;
import wander.wise.application.service.api.ai.AiApiService;
import wander.wise.application.service.api.images.ImageSearchApiService;
import wander.wise.application.service.api.maps.MapsApiService;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private static final double EARTH_RADIUS_KM = 6371;
    private static final int INITIAL_ATTEMPTS = 0;
    private static final int MAX_ATTEMPTS = 1;
    private final AiApiService aiApiService;
    private final ImageSearchApiService imageSearchApiService;
    private final MapsApiService mapsApiService;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardSpecificationBuilder cardSpecificationBuilder;

    @Override
    public CardWithoutDistanceDto saveCard(CreateCardRequestDto requestDto) {
        return null;
    }

    @Override
    public List<CardWithoutDistanceDto> saveAll(List<CreateCardRequestDto> requestDtos) {
        return List.of();
    }

    @Override
    public List<CardDto> search(
            Pageable pageable,
            CardSearchParameters searchParams) {
        Specification<Card> cardSpec = cardSpecificationBuilder.build(searchParams);
        List<Card> foundCards = findOrGenerateCards(
                searchParams,
                cardSpec,
                pageable,
                INITIAL_ATTEMPTS);
        MapsResponseDto startLocationCoordinates = mapsApiService
                .getMapsResponseByLocationName(searchParams.startLocation());
        return initializeCardDtos(
                foundCards,
                startLocationCoordinates);
    }

    private List<Card> findOrGenerateCards(
            CardSearchParameters searchParams,
            Specification<Card> cardSpec,
            Pageable pageable,
            int attempts) {
        List<Card> foundCards = findCards(cardSpec);
        if (foundCards.size() < getRequiredCardsAmount(pageable)
                && isAiCardsRequired(searchParams)
                && attempts < MAX_ATTEMPTS) {
            attempts++;
            generateAndSaveCards(
                    searchParams,
                    getLocationsToExcludeAndTypeMap(
                            searchParams,
                            foundCards));
            return findOrGenerateCards(
                    searchParams,
                    cardSpec,
                    pageable,
                    attempts);
        }
        if (foundCards.isEmpty()) {
            throw new CardSearchException("Couldn't find and generate enough cards, "
                    + "that match provided requirements");
        }
        return getPage(pageable, foundCards);
    }

    private List<Card> findCards(Specification<Card> cardSpec) {
        return cardRepository.findAll(cardSpec)
                .stream()
                .filter(card -> card.isShown() == true)
                .toList();
    }

    private void generateAndSaveCards(
            CardSearchParameters searchParams,
            Map<String, List<String>> locationsToExcludeAndTypeMap) {
        List<AiResponseDto> responseDtos = aiApiService.getAiResponses(
                searchParams,
                locationsToExcludeAndTypeMap);
        List<Card> generatedCards = aiResponsesToCards(responseDtos);
        if (!generatedCards.isEmpty()) {
            cardRepository.saveAll(generatedCards);
        }
    }

    private List<Card> aiResponsesToCards(List<AiResponseDto> responseDtos) {
        List<Card> generatedCards = responseDtos.stream()
                .map(this::initialiseCard)
                .filter(Objects::nonNull)
                .toList();
        return generatedCards;
    }

    private Card initialiseCard(AiResponseDto aiResponseDto) {
        String fullName = aiResponseDto.fullName();
        if (cardRepository.existsByFullName(fullName)) {
            return null;
        }
        String searchKey = getSearchKey(fullName);
        MapsResponseDto mapsResponseDto = mapsApiService
                .getMapsResponseByLocationName(searchKey);
        if (isValidLocation(mapsResponseDto)) {
            return fillNewCard(
                    aiResponseDto,
                    searchKey,
                    mapsResponseDto);
        }
        return null;
    }

    private Card fillNewCard(
            AiResponseDto aiResponseDto,
            String searchKey,
            MapsResponseDto mapsResponseDto) {
        String imageLinks = imageSearchApiService.getImageLinks(searchKey);
        Card newCard = cardMapper.aiResponseToCard(aiResponseDto);
        newCard.setImageLinks(imageLinks);
        newCard.setMapLink(mapsResponseDto.mapLink());
        newCard.setLatitude(mapsResponseDto.latitude());
        newCard.setLongitude(mapsResponseDto.longitude());
        return newCard;
    }

    private List<CardDto> initializeCardDtos(
            List<Card> foundCards,
            MapsResponseDto startLocationCoordinates) {
        return foundCards.stream()
                .map(card -> {
                    CardDto cardDto = cardMapper.toDto(card);
                    cardDto.setDistance(findDistance(card, startLocationCoordinates));
                    return cardDto;
                })
                .toList();
    }

    private int findDistance(
            Card card,
            MapsResponseDto startLocationCoordinates) {
        // Parse coordinates
        double startLatitude = startLocationCoordinates.latitude();
        double startLongitude = startLocationCoordinates.longitude();
        double endLatitude = card.getLatitude();
        double endLongitude = card.getLongitude();
        // Calculate difference
        double latitudeDifference = Math.toRadians(endLatitude - startLatitude);
        double longitudeDifference = Math.toRadians(endLongitude - startLongitude);
        // Find distance
        double a = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                + Math.cos(Math.toRadians(startLatitude)) * Math.cos(Math.toRadians(endLatitude))
                * Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) (EARTH_RADIUS_KM * c);
    }

    private static Map<String, List<String>> getLocationsToExcludeAndTypeMap(
            CardSearchParameters searchParams,
            List<Card> foundCards) {
        // TODO: fix method implementation. Now it creates map with 1 empty key
        Map<String, List<String>> locationsToExcludeAndTypeMap = new HashMap<>();
        Arrays.stream(searchParams.tripTypes()).forEach(type -> {
            locationsToExcludeAndTypeMap.put(type, new ArrayList<>());
            foundCards.forEach(card -> {
                if (card.getTripTypes().contains(type)) {
                    locationsToExcludeAndTypeMap.get(type).add(card.getFullName());
                }
            });
        });
        return locationsToExcludeAndTypeMap;
    }

    private static List<Card> getPage(
            Pageable pageable,
            List<Card> foundCards) {
        int pageStart = (int) pageable.getOffset();
        int pageEnd = Math.min(pageStart + pageable.getPageSize(), foundCards.size());
        return new PageImpl<>(
                foundCards.subList(pageStart, pageEnd),
                pageable,
                foundCards.size()).toList();
    }

    private static int getRequiredCardsAmount(Pageable pageable) {
        int requiredAmount = pageable.getPageSize() * (pageable.getPageNumber() + 1);
        return requiredAmount;
    }

    private static boolean isAiCardsRequired(CardSearchParameters searchParams) {
        return Arrays.toString(searchParams.author()).contains("AI");
    }

    private static String getExcludeLocationName(Card card) {
        String[] fullNameArray = card.getFullName().split("\\|");
        return new StringBuilder()
                .append(fullNameArray[0])
                .append(" (")
                .append(fullNameArray[1])
                .append(")")
                .toString();
    }

    private static String getSearchKey(String fullName) {
        String[] searchKeyArray = fullName.split("\\|");
        String searchKey = new StringBuilder()
                .append(searchKeyArray[0])
                .append(" ")
                .append(searchKeyArray[1])
                .toString();
        return searchKey;
    }

    private static boolean isValidLocation(MapsResponseDto mapsResponseDto) {
        return mapsResponseDto.latitude() != 0
                || mapsResponseDto.longitude() != 0;
    }
}
