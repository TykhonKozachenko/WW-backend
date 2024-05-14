package wander.wise.application.service.card;

import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;
import wander.wise.application.dto.ai.AiResponseDto;
import wander.wise.application.dto.card.CardDto;
import wander.wise.application.dto.card.CardSearchParameters;
import wander.wise.application.dto.card.CreateCardRequestDto;
import wander.wise.application.dto.card.ReportCardRequestDto;
import wander.wise.application.dto.card.SearchCardsResponseDto;
import wander.wise.application.dto.maps.LocationDto;
import wander.wise.application.exception.custom.AuthorizationException;
import wander.wise.application.exception.custom.CardSearchException;
import wander.wise.application.mapper.CardMapper;
import wander.wise.application.model.Card;
import wander.wise.application.model.Collection;
import wander.wise.application.model.User;
import wander.wise.application.repository.card.CardRepository;
import wander.wise.application.repository.card.CardSpecificationBuilder;
import wander.wise.application.repository.collection.CollectionRepository;
import wander.wise.application.repository.user.UserRepository;
import wander.wise.application.service.api.ai.AiApiService;
import wander.wise.application.service.api.email.EmailService;
import wander.wise.application.service.api.images.ImageSearchApiService;
import wander.wise.application.service.api.maps.MapsApiService;
import wander.wise.application.service.api.storage.StorageService;

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
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CollectionRepository collectionRepository;
    private final StorageService storageService;

    @Override
    public CardDto createNewCard(String email, CreateCardRequestDto requestDto) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));
        if (!author.isBanned()) {
            Card savedCard = cardRepository.save(initializeUsersCard(requestDto, author));
            Collection updatedSavedCards = collectionRepository.findAllByUserEmail(email)
                    .stream()
                    .filter(collection -> collection.getName().equals("Created cards"))
                    .findFirst()
                    .get();
            updatedSavedCards.getCards().add(savedCard);
            collectionRepository.save(updatedSavedCards);
            return cardMapper.toDto(savedCard);
        } else {
            throw new AuthorizationException("Access denied. User is banned.");
        }
    }

    @Override
    public CardDto updateById(Long id, String email, CreateCardRequestDto requestDto) {
        Card updatedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        User updatingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));
        if (updatingUser.getAuthorities().size() > 1
                || (updatedCard.getAuthor()
                .equals(updatingUser.getPseudonym())
                && !updatingUser.isBanned())) {
            updatedCard = cardMapper.updateCardFromRequestDto(updatedCard, requestDto);
            if (updatedCard.getImageLinks().length() > 0) {
                Arrays.stream(updatedCard.getImageLinks().split("\\|"))
                        .filter(link -> !Arrays.stream(requestDto
                                .imageLinks()).toList().contains(link))
                        .forEach(link -> storageService.deleteFile(link
                                .substring(link.lastIndexOf("/") + 1)));
            }
            updatedCard.setImageLinks(String.join("|", requestDto.imageLinks()));
            return cardMapper.toDto(cardRepository.save(updatedCard));
        } else {
            throw new AuthorizationException("Access denied.");
        }
    }

    @Override
    public CardDto addImagesToCardById(Long id, String email, List<MultipartFile> images) {
        Card updatedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        User updatingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));
        if (updatingUser.getAuthorities().size() > 1
                || (updatedCard.getAuthor()
                .equals(updatingUser.getPseudonym())
                && !updatingUser.isBanned())) {
            String imageLinks = String.join("|", images.stream()
                    .map(storageService::uploadFile)
                    .toList());
            String existingLinks = updatedCard.getImageLinks();
            if (!existingLinks.isEmpty()) {
                existingLinks = existingLinks + "|" + imageLinks;
            } else {
                existingLinks = imageLinks;
            }
            updatedCard.setImageLinks(existingLinks);
            return cardMapper.toDto(cardRepository.save(updatedCard));
        } else {
            throw new AuthorizationException("Access denied.");
        }
    }

    @Override
    public CardDto findById(Long id) {
        return cardMapper.toDto(cardRepository.findById(id)
                .filter(Card::isShown).orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id)));
    }

    @Override
    public CardDto findByIdAsAdmin(Long id) {
        return cardMapper.toDto(cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id)));
    }

    @Override
    public boolean addCardToSaved(Long id, String email) {
        Card addedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        Collection updatedSavedCards = collectionRepository.findAllByUserEmail(email)
                .stream()
                .filter(collection -> collection.getName().equals("Saved cards"))
                .findFirst()
                .get();
        List<Long> savedCardsIds = updatedSavedCards.getCards().stream().map(Card::getId).toList();
        if (!savedCardsIds.contains(id)) {
            updatedSavedCards.getCards().add(addedCard);
            collectionRepository.save(updatedSavedCards);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeCardFromSaved(Long id, String email) {
        Card removedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        Collection updatedSavedCards = collectionRepository.findAllByUserEmail(email)
                .stream()
                .filter(collection -> collection.getName().equals("Saved cards"))
                .findFirst()
                .get();
        List<Long> savedCardsIds = updatedSavedCards.getCards().stream().map(Card::getId).toList();
        if (savedCardsIds.contains(id)) {
            updatedSavedCards.getCards().remove(removedCard);
            collectionRepository.save(updatedSavedCards);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean postLike(Long id, String email) {
        Collection updatedLikedCards = collectionRepository.findAllByUserEmail(email)
                .stream()
                .filter(collection -> collection.getName().equals("Liked cards"))
                .findFirst()
                .get();
        Card likedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        if (!updatedLikedCards.getCards().contains(likedCard)) {
            likedCard.setLikes(likedCard.getLikes() + 1);
            Card savedCard = cardRepository.save(likedCard);
            updatedLikedCards.getCards().add(likedCard);
            collectionRepository.save(updatedLikedCards);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeLike(Long id, String email) {
        Card likedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        Collection updatedLikedCards = collectionRepository.findAllByUserEmail(email)
                .stream()
                .filter(collection -> collection.getName().equals("Liked cards"))
                .findFirst()
                .get();
        if (updatedLikedCards.getCards().contains(likedCard)) {
            updatedLikedCards.getCards().remove(likedCard);
            collectionRepository.save(updatedLikedCards);
            likedCard.setLikes(likedCard.getLikes() - 1);
            cardRepository.save(likedCard);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hideCard(Long id) {
        Card hiddenCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        if (hiddenCard.isShown()) {
            hiddenCard.setShown(false);
            cardRepository.save(hiddenCard);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean revealCard(Long id) {
        Card revealedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        if (!revealedCard.isShown()) {
            revealedCard.setShown(true);
            cardRepository.save(revealedCard);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void deleteById(Long id, String email) {
        Card updatedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        User updatingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));
        if (updatingUser.getAuthorities().size() > 1
                || updatedCard.getAuthor()
                .equals(updatingUser.getPseudonym())) {
            cardRepository.deleteById(id);
        } else {
            throw new AuthorizationException("Access denied.");
        }
    }

    @Override
    public void report(Long id, String email, ReportCardRequestDto requestDto) {
        Card reportedCard = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find card by id: " + id));
        reportedCard.setReports(reportedCard.getReports() + 1);
        String message = new StringBuilder()
                .append("User email: ").append(email)
                .append(System.lineSeparator())
                .append("Card id: ").append(id)
                .append(System.lineSeparator())
                .append("Report text: ").append(requestDto.text())
                .append(System.lineSeparator())
                .append("Card was reported: ")
                .append(reportedCard.getReports()).append(" times")
                .toString();
        emailService.sendEmail(
                "budzetbudzet4@gmail.com",
                "Report for card " + reportedCard.getFullName(),
                message);
        cardRepository.save(reportedCard);
    }

    @Override
    public SearchCardsResponseDto search(
            Pageable pageable,
            CardSearchParameters searchParams) {
        searchParams = resetTravelDistance(searchParams);
        Specification<Card> cardSpec = cardSpecificationBuilder.build(searchParams);
        List<Card> foundCards = findOrGenerateCards(
                searchParams,
                cardSpec,
                pageable,
                INITIAL_ATTEMPTS);
        LocationDto startLocationCoordinates = mapsApiService
                .getMapsResponseByLocationName(searchParams.startLocation());

        return new SearchCardsResponseDto(
                pageable.getPageNumber(),
                initializeCardDtos(
                        foundCards,
                        startLocationCoordinates));
    }

    private CardSearchParameters resetTravelDistance(CardSearchParameters searchParameters) {
        switch (searchParameters.travelDistance()[0]) {
            case "Populated locality" ->
                    searchParameters = searchParameters
                            .setTravelDistance(searchParameters.startLocation()
                                    .split(",")[0]);
            case "Country" ->
                    searchParameters = searchParameters
                            .setTravelDistance(searchParameters.startLocation()
                                    .split(",")[1]);
            case "Region" ->
                    searchParameters = aiApiService.defineRegion(searchParameters);
            case "Continent" ->
                    searchParameters = aiApiService.defineContinent(searchParameters);
            default ->
                    searchParameters = searchParameters.setTravelDistance("");
        }
        return searchParameters;
    }

    private List<Card> findOrGenerateCards(
            CardSearchParameters searchParams,
            Specification<Card> cardSpec,
            Pageable pageable,
            int attempts) {
        List<Card> foundCards = findCards(cardSpec).stream()
                .filter(Card::isShown)
                .toList();
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
                .filter(Card::isShown)
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
            for (Card card : generatedCards) {
                try {
                    cardRepository.save(card);
                } catch (Exception e) {
                    System.out.println("Duplicate entity");
                }
            }
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
        LocationDto locationDto = mapsApiService
                .getMapsResponseByLocationName(searchKey);
        if (isValidLocation(locationDto)) {
            return fillNewCard(
                    aiResponseDto,
                    searchKey,
                    locationDto);
        }
        return null;
    }

    private Card fillNewCard(
            AiResponseDto aiResponseDto,
            String searchKey,
            LocationDto locationDto) {
        String imageLinks = imageSearchApiService.getImageLinks(searchKey);
        Card newCard = cardMapper.aiResponseToCard(aiResponseDto);
        newCard.setImageLinks(imageLinks);
        newCard.setMapLink(locationDto.mapLink());
        newCard.setLatitude(locationDto.latitude());
        newCard.setLongitude(locationDto.longitude());
        return newCard;
    }

    private List<CardDto> initializeCardDtos(
            List<Card> foundCards,
            LocationDto startLocationCoordinates) {
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
            LocationDto startLocationCoordinates) {
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

    private Card initializeUsersCard(CreateCardRequestDto requestDto, User author) {
        Card newCard = cardMapper.toModel(requestDto);
        newCard.setAuthor(author.getPseudonym());
        LocationDto locationDto = mapsApiService.getMapsResponseByUsersUrl(newCard.getMapLink());
        newCard.setMapLink(locationDto.mapLink());
        newCard.setLatitude(locationDto.latitude());
        newCard.setLongitude(locationDto.longitude());
        return newCard;
    }

    private static Map<String, List<String>> getLocationsToExcludeAndTypeMap(
            CardSearchParameters searchParams,
            List<Card> foundCards) {
        Map<String, List<String>> locationsToExcludeAndTypeMap = new HashMap<>();
        Arrays.stream(searchParams.tripTypes()).forEach(type -> {
            locationsToExcludeAndTypeMap.put(type, new ArrayList<>());
            foundCards.forEach(card -> {
                if (card.getTripTypes().contains(type)) {
                    locationsToExcludeAndTypeMap.get(type).add(getExcludeLocationName(card));
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
        return pageable.getPageSize() * (pageable.getPageNumber() + 1);
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

    private static boolean isValidLocation(LocationDto locationDto) {
        return locationDto.latitude() != 0
                || locationDto.longitude() != 0;
    }
}
