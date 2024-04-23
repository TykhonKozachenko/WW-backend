package wander.wise.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import wander.wise.application.dto.collection.CollectionDto;
import wander.wise.application.dto.collection.CreateCollectionRequestDto;
import wander.wise.application.dto.collection.UpdateCollectionRequestDto;
import wander.wise.application.model.Collection;
import wander.wise.application.service.collection.CollectionService;

import java.util.Set;

@Tag(name = "Collection management endpoints")
@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionService collectionService;

    @Operation(
            summary = "Save Collection",
            description = "Creates a new collection "
                    + "with the specified cards for the "
                    + "authenticated user. "
                    + "Only users with 'USER' authority "
                    + "can access this endpoint. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"userId\": 1,\n\n"
                    + "  \"cardIds\": [101, 102, 103]\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 10,\n\n"
                    + "  \"author\": \"John Doe\",\n\n"
                    + "  \"name\": \"My Collection\",\n\n"
                    + "  \"imageLink\": \"http://example.com/image.jpg\",\n\n"
                    + "  \"cardWithoutDistanceDtos\": [\n\n"
                    + "    // card details\n\n"
                    + "  ],\n\n"
                    + "  \"isPublic\": true\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: AccessDeniedException "
                    + "(if the user does not have 'USER' authority), "
                    + "ValidationException (if the request body "
                    + "does not pass validation checks), "
                    + "EntityNotFoundException (if the user "
                    + "or cards are not found)"
    )
    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public CollectionDto save(Authentication authentication, @Valid @RequestBody CreateCollectionRequestDto requestDto) {
        return collectionService.save(authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Find Collection by ID",
            description = "Retrieves a collection "
                    + "by its unique identifier. "
                    + "This endpoint can only be accessed "
                    + "by users with 'USER' authority. "
                    + "The collection is returned if it is "
                    + "public or if the requesting user is the "
                    + "owner of the collection. "
                    + "\n\n\n\n**Request Parameter Example:**\n\n"
                    + "id: 42\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 42,\n\n"
                    + "  \"author\": \"Jane Doe\",\n\n"
                    + "  \"name\": \"Jane's Collection\",\n\n"
                    + "  \"imageLink\": \"http://example.com/"
                    + "jane_collection.jpg\",\n\n"
                    + "  \"cardWithoutDistanceDtos\": [\n\n"
                    + "    // card details\n\n"
                    + "  ],\n\n"
                    + "  \"isPublic\": false\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if no collection with the given ID is found), "
                    + "AuthorizationException (if the collection "
                    + "is private and the user is not the owner)"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public CollectionDto findById(@PathVariable Long id, Authentication authentication) {
        return collectionService.findById(id, authentication.getName());
    }

    @Operation(
            summary = "Update Collection by ID",
            description = "Updates an existing collection "
                    + "with the given ID. "
                    + "This endpoint is accessible only to "
                    + "users with 'USER' authority. "
                    + "It allows updating the collection's "
                    + "name, the set of card IDs, and its "
                    + "public visibility status. "
                    + "Default collections such as 'Liked cards', "
                    + "'Created cards', and 'Saved cards' "
                    + "cannot be modified. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"name\": \"My Updated Collection\",\n\n"
                    + "  \"cardIds\": [201, 202, 203],\n\n"
                    + "  \"isPublic\": true\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 15,\n\n"
                    + "  \"author\": \"John Doe\",\n\n"
                    + "  \"name\": \"My Updated Collection\",\n\n"
                    + "  \"imageLink\": \"http://example.com/"
                    + "updated_collection.jpg\",\n\n"
                    + "  \"cardWithoutDistanceDtos\": [\n\n"
                    + "    // updated card details\n\n"
                    + "  ],\n\n"
                    + "  \"isPublic\": true\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the collection with the given ID is not found), "
                    + "AuthorizationException (if the user is not "
                    + "authorized to update the collection or "
                    + "attempts to modify a default collection)"
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public CollectionDto updateById(@PathVariable Long id, Authentication authentication, @Valid @RequestBody UpdateCollectionRequestDto requestDto) {
        return collectionService.updateById(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Delete Collection by ID",
            description = "Deletes a collection " 
                    + "with the specified ID. " 
                    + "This endpoint is restricted " 
                    + "to users with 'USER' authority. " 
                    + "It does not allow deletion of " 
                    + "default collections such as 'Liked " 
                    + "cards', 'Created cards', and 'Saved cards'. " 
                    + "\n\n\n\n**Path Variable Example:**\n\n" 
                    + "id: 123\n\n\n\n" 
                    + "Roles with access: USER\n\n\n\n" 
                    + "Possible exceptions: EntityNotFoundException " 
                    + "(if the collection with the given ID is not found), " 
                    + "AuthorizationException (if the user is not " 
                    + "authorized to delete the collection or " 
                    + "attempts to delete a default collection)"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteById(@PathVariable Long id, Authentication authentication) {
        collectionService.deleteById(id, authentication.getName());
    }
}
