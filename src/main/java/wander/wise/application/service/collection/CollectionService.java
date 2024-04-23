package wander.wise.application.service.collection;

import wander.wise.application.dto.collection.CollectionDto;
import wander.wise.application.dto.collection.CreateCollectionRequestDto;
import wander.wise.application.dto.collection.UpdateCollectionRequestDto;

public interface CollectionService {
    CollectionDto save(String email, CreateCollectionRequestDto requestDto);

    CollectionDto findById(Long id, String email);

    CollectionDto updateById(Long id, String email, UpdateCollectionRequestDto requestDto);

    void deleteById(Long id, String email);
}
