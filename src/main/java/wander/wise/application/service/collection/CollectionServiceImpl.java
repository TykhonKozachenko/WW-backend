package wander.wise.application.service.collection;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wander.wise.application.dto.collection.CollectionDto;
import wander.wise.application.dto.collection.CreateCollectionRequestDto;
import wander.wise.application.dto.collection.UpdateCollectionRequestDto;
import wander.wise.application.exception.custom.AuthorizationException;
import wander.wise.application.mapper.CollectionMapper;
import wander.wise.application.model.Card;
import wander.wise.application.model.Collection;
import wander.wise.application.model.User;
import wander.wise.application.repository.collection.CollectionRepository;
import wander.wise.application.service.user.UserService;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;
    private final UserService userService;

    @Override
    public CollectionDto save(String email, CreateCollectionRequestDto requestDto) {
        User updatedUser = userService.findUserAndAuthorize(requestDto.userId(), email);
        Collection savedCollection = collectionMapper.toModel(requestDto);
        savedCollection.setUser(updatedUser);
        savedCollection = collectionRepository.save(savedCollection);
        savedCollection = collectionRepository.findById(savedCollection.getId()).get();
        savedCollection.setName(savedCollection.getCards()
                .stream()
                .limit(3)
                .map(Card::getFullName)
                .map(name -> name.split("\\|")[0])
                .collect(Collectors.joining(",")) + "...");
        return collectionMapper.toDto(collectionRepository.save(savedCollection));
    }

    @Override
    public CollectionDto findById(Long id, String email) {
        Collection foudCollection = collectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find collection by id: " + id));
        if (foudCollection.isPublic() || foudCollection.getUser().getEmail().equals(email)) {
            return collectionMapper.toDto(foudCollection);
        } else {
            throw new AuthorizationException("You don't have access for this collection. "
                    + "Ask it's owner to make it public.");
        }
    }

    @Override
    public CollectionDto updateById(Long id, String email, UpdateCollectionRequestDto requestDto) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find collection by id: " + id));
        userService.findUserAndAuthorize(collection.getUser().getId(), email);
        if (!collection.getName().equals("Liked cards")
                && !collection.getName().equals("Created cards")
                && !collection.getName().equals("Saved cards")) {
            collection = collectionMapper.updateCollectionFromDto(collection, requestDto);
            collection.setPublic(requestDto.isPublic());
            return collectionMapper.toDto(collectionRepository.save(collection));
        } else {
            throw new AuthorizationException("Access denied. You can't "
                    + "change default collections.");
        }
    }

    @Override
    public void deleteById(Long id, String email) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find collection by id: " + id));
        userService.findUserAndAuthorize(collection.getUser().getId(), email);
        if (!collection.getName().equals("Liked cards")
                && !collection.getName().equals("Created cards")
                && !collection.getName().equals("Saved cards")) {
            collectionRepository.deleteById(id);
        } else {
            throw new AuthorizationException("Access denied. You can't "
                    + "delete default collections.");
        }
    }
}
