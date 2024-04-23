package wander.wise.application.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import wander.wise.application.dto.card.*;
import wander.wise.application.service.card.CardService;

@Tag(name = "Card management endpoints")
@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @Operation(
            summary = "Search Cards",
            description = "Allows searching for cards based "
                    + "on various parameters. Can be accessed "
                    + "by unauthorized users. "
                    + "Returns a paginated list of CardDto "
                    + "objects that match the criteria. "
                    + "\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"startLocation\": \"Tokyo\",\n\n"
                    + "  \"tripTypes\": [\"Adventure\", \"Nature\"],\n\n"
                    + "  \"climate\": [\"Temperate\"],\n\n"
                    + "  \"specialRequirements\": [\"Pet-friendly\", \"Disability access\"],\n\n"
                    + "  \"travelDistance\": [\"Japan\"],\n\n"
                    + "  \"author\": [\"AI\"]\n\n"
                    + "}\n\n\n\n"
                    + "\n\n**CORRECT JSON FOR PAGEABLE:**\n\n"
                    + "{\n\n"
                    + "  \"page\": 0,\n\n"
                    + "  \"size\": 8,\n\n"
                    + "  \"sort\": \"asc\"\n\n"
                    + "}\n\n\n\n"
                    + "Author field can have 3 configurations: "
                    + "[\"AI\"] - only generated cards, [\"User\"] "
                    + "- only custom cards, [] - cards by all authors"
                    + "\n\n**Response Body Example:**\n\n"
                    + "[\n\n"
                    + "  {\n\n"
                    + "    \"id\": 1,\n\n"
                    + "    \"name\": \"Mount Fuji Adventure\",\n\n"
                    + "    \"author\": \"Author1\",\n\n"
                    + "    \"tripTypes\": [\"Active\", \"Nature\"],\n\n"
                    + "    \"climate\": \"Temperate\",\n\n"
                    + "    \"specialRequirements\": [\"Hiking gear required\"],\n\n"
                    + "    \"whereIs\": \"Shizuoka, Japan\",\n\n"
                    + "    \"description\": \"A thrilling journey to the peak of Mount Fuji...\",\n\n"
                    + "    \"whyThisPlace\": [\"UNESCO World Heritage site\", \"Iconic mountain\"],\n\n"
                    + "    \"imageLinks\": [\"linkToImage1.jpg\", \"linkToImage2.jpg\"],\n\n"
                    + "    \"mapLink\": \"linkToMap\",\n\n"
                    + "    \"distance\": 100,\n\n"
                    + "    \"likes\": 250,\n\n"
                    + "    \"comments\": [CommentDto1, CommentDto2]\n\n"
                    + "    \"shown\": true\n\n"
                    + "  },\n\n"
                    + "  // More CardDto objects\n\n"
                    + "]\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `CardSearchException` there is not enough "
                    + "cards in db and ai also can't generate enough.\n\n"
                    + "- `AiException` ai returns incorrect data."
    )
    @PostMapping("/search")
    List<CardDto> search(@RequestBody CardSearchParameters searchParameters, Pageable pageable) {
        return cardService.search(pageable, searchParameters);
    }

    @Operation(
            summary = "Find Card by ID",
            description = "Retrieves a card's details "
                    + "without distance information by its ID. "
                    + "Only users with roles 'ROLE_USER' "
                    + "or 'ROLE_ADMIN' can access this endpoint. "
                    + "\n\n\n\n**Request:**\n\n"
                    + "GET /details/{id}\n\n"
                    + "Path Variable: id (Long) - The unique "
                    + "identifier of the card.\n\n"
                    + "**Response Body Example:**\n\n"
                    + "[\n\n"
                    + "  {\n\n"
                    + "    \"id\": 1,\n\n"
                    + "    \"name\": \"Mount Fuji Adventure\",\n\n"
                    + "    \"author\": \"Author1\",\n\n"
                    + "    \"tripTypes\": [\"Active\", \"Nature\"],\n\n"
                    + "    \"climate\": \"Temperate\",\n\n"
                    + "    \"specialRequirements\": [\"Hiking gear required\"],\n\n"
                    + "    \"whereIs\": \"Shizuoka, Japan\",\n\n"
                    + "    \"description\": \"A thrilling journey to the peak of Mount Fuji...\",\n\n"
                    + "    \"whyThisPlace\": [\"UNESCO World Heritage site\", \"Iconic mountain\"],\n\n"
                    + "    \"imageLinks\": [\"linkToImage1.jpg\", \"linkToImage2.jpg\"],\n\n"
                    + "    \"mapLink\": \"linkToMap\",\n\n"
                    + "    \"likes\": 250,\n\n"
                    + "    \"comments\": [CommentDto1, CommentDto2]\n\n"
                    + "    \"shown\": true\n\n"
                    + "  }\n\n"
                    + "\n\n**Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the "
                    + "card with the given ID does not exist "
                    + "or is not shown."
    )
    @GetMapping("/details/{id}")
    public CardWithoutDistanceDto findById(@PathVariable Long id) {
        return cardService.findById(id);
    }

    @Operation(
            summary = "Retrieve Card Details as Admin",
            description = "Fetches the complete details "
                    + "of a card by its ID, accessible only "
                    + "by users with 'ADMIN' authority. "
                    + "This admin-specific endpoint bypasses "
                    + "the 'shown' property check, allowing "
                    + "access to all cards regardless of their "
                    + "visibility status. "
                    + "\n\n\n\n**Request:**\n\n"
                    + "GET /as-admin/{id}\n\n"
                    + "Path Variable: id (Long) - The unique "
                    + "identifier of the card to retrieve.\n\n"
                    + "**Response Body Example:**\n\n"
                    + "[\n\n"
                    + "  {\n\n"
                    + "    \"id\": 1,\n\n"
                    + "    \"name\": \"Mount Fuji Adventure\",\n\n"
                    + "    \"author\": \"Author1\",\n\n"
                    + "    \"tripTypes\": [\"Active\", \"Nature\"],\n\n"
                    + "    \"climate\": \"Temperate\",\n\n"
                    + "    \"specialRequirements\": [\"Hiking gear required\"],\n\n"
                    + "    \"whereIs\": \"Shizuoka, Japan\",\n\n"
                    + "    \"description\": \"A thrilling journey to the peak of Mount Fuji...\",\n\n"
                    + "    \"whyThisPlace\": [\"UNESCO World Heritage site\", \"Iconic mountain\"],\n\n"
                    + "    \"imageLinks\": [\"linkToImage1.jpg\", \"linkToImage2.jpg\"],\n\n"
                    + "    \"mapLink\": \"linkToMap\",\n\n"
                    + "    \"likes\": 250,\n\n"
                    + "    \"comments\": [CommentDto1, CommentDto2]\n\n"
                    + "    \"shown\": true\n\n"
                    + "  }\n\n"
                    + "\n\n**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if no card "
                    + "is found with the provided ID."
    )
    @GetMapping("/as-admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CardWithoutDistanceDto findByIdAsAdmin(@PathVariable Long id) {
        return cardService.findByIdAsAdmin(id);
    }

    @Operation(
            summary = "Create a New Card",
            description = "Allows a user with 'USER' "
                    + "authority to create a new card. "
                    + "The card details are provided in "
                    + "the request body. "
                    + "Upon successful creation, the card "
                    + "details are returned without distance "
                    + "information. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"fullName\": \"Location name|Populated "
                    + "locality|Region|Country|Continent\",\n\n"
                    + "  \"tripTypes\": \"Type|Type|Type\",\n\n"
                    + "  \"climate\": \"Climate\",\n\n"
                    + "  \"specialRequirements\": \"Requirement|Requirement\",\n\n"
                    + "  \"description\": \"Description\",\n\n"
                    + "  \"whyThisPlace\": \"Reason|Reason|Reason\",\n\n"
                    + "  \"imageLinks\": \"Link|Link|Link\",\n\n"
                    + "  \"mapLink\": \"Link\"\n\n"
                    + "}\n\n"
                    + "**Response Body Example:**\n\n"
                    + "[\n\n"
                    + "  {\n\n"
                    + "    \"id\": 1,\n\n"
                    + "    \"name\": \"Mount Fuji Adventure\",\n\n"
                    + "    \"author\": \"Author1\",\n\n"
                    + "    \"tripTypes\": [\"Active\", \"Nature\"],\n\n"
                    + "    \"climate\": \"Temperate\",\n\n"
                    + "    \"specialRequirements\": [\"Hiking gear required\"],\n\n"
                    + "    \"whereIs\": \"Shizuoka, Japan\",\n\n"
                    + "    \"description\": \"A thrilling journey to "
                    + "the peak of Mount Fuji...\",\n\n"
                    + "    \"whyThisPlace\": [\"UNESCO World "
                    + "Heritage site\", \"Iconic mountain\"],\n\n"
                    + "    \"imageLinks\": [\"linkToImage1.jpg\", "
                    + "\"linkToImage2.jpg\"],\n\n"
                    + "    \"mapLink\": \"linkToMap\",\n\n"
                    + "    \"likes\": 250,\n\n"
                    + "    \"comments\": [CommentDto1, "
                    + "CommentDto2]\n\n"
                    + "    \"shown\": true\n\n"
                    + "  }\n\n"
                    + "\n\n**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the "
                    + "user's email is not found.\n\n"
                    + "- `AuthorizationException` if the "
                    + "user is banned and therefore denied access."
    )
    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public CardWithoutDistanceDto createNewCard(Authentication authentication, @Valid @RequestBody CreateCardRequestDto requestDto) {
        return cardService.createNewCard(authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Add Card to \"Saved cards\" collection",
            description = "Allows a user with 'USER' authority "
                    + "to add a specific card to their 'Saved cards' "
                    + "collection. "
                    + "The card is identified by its ID, which "
                    + "is provided in the path variable. "
                    + "This operation does not return a body, but "
                    + "it updates the user's collection of saved cards. "
                    + "\n\n\n\n**Request:**\n\n"
                    + "PUT /add-to-saved/{id}\n\n"
                    + "Path Variable: id (Long) - The unique identifier "
                    + "of the card to be added to the saved collection.\n\n"
                    + "\n\n**Response:**\n\n"
                    + "This endpoint does not return a response body. "
                    + "On successful execution, it will result in an "
                    + "HTTP 200 OK status. "
                    + "\n\n\n\n**Roles with Access:**\n\n"
                    + "- 'USER'\n\n"
                    + "\n\n**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the card with "
                    + "the given ID is not found."
    )
    @PutMapping("/add-to-saved/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void addCardToSaved(@PathVariable Long id, Authentication authentication) {
        cardService.addCardToSaved(id, authentication.getName());
    }

    @Operation(
            summary = "Remove Card from \"Saved cards\" collection",
            description = "Enables a user with 'USER' "
                    + "authority to remove a card from their "
                    + "'Saved cards' collection. "
                    + "The card to be removed is identified "
                    + "by its ID, provided in the path variable. "
                    + "This operation does not return a body, "
                    + "but it updates the user's collection by "
                    + "removing the specified card. "
                    + "\n\n\n\n**Request:**\n\n"
                    + "PUT /remove-from-saved/{id}\n\n"
                    + "Path Variable: id (Long) - The unique "
                    + "identifier of the card to be removed from "
                    + "the saved collection.\n\n"
                    + "\n\n**Response:**\n\n"
                    + "This endpoint does not return a response "
                    + "body. On successful execution, it will "
                    + "result in an HTTP 200 OK status. "
                    + "\n\n\n\n**Roles with Access:**\n\n"
                    + "- 'USER'\n\n"
                    + "\n\n**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the card "
                    + "with the given ID is not found in the user's "
                    + "'Saved cards' collection."
    )
    @PutMapping("/remove-from-saved/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void removeCardFromSaved(@PathVariable Long id, Authentication authentication) {
        cardService.removeCardFromSaved(id, authentication.getName());
    }

    @Operation(
            summary = "Update Card Details",
            description = "Updates the details of an existing "
                    + "card by its ID for users with 'USER' "
                    + "or higher authority. "
                    + "The request body must contain the updated "
                    + "card information. "
                    + "Only the card's author or users with "
                    + "multiple authorities can update the card, "
                    + "provided the user is not banned. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"fullName\": \"Location name|Populated "
                    + "locality|Region|Country|Continent\",\n\n"
                    + "  \"tripTypes\": \"Type|Type|Type\",\n\n"
                    + "  \"climate\": \"Climate\",\n\n"
                    + "  \"specialRequirements\": \"Requirement|Requirement\",\n\n"
                    + "  \"description\": \"Description\",\n\n"
                    + "  \"whyThisPlace\": \"Reason|Reason|Reason\",\n\n"
                    + "  \"imageLinks\": \"Link|Link|Link\",\n\n"
                    + "  \"mapLink\": \"Link\"\n\n"
                    + "}\n\n"
                    + "**Response Body Example:**\n\n"
                    + "[\n\n"
                    + "  {\n\n"
                    + "    \"id\": 1,\n\n"
                    + "    \"name\": \"Mount Fuji Adventure\",\n\n"
                    + "    \"author\": \"Author1\",\n\n"
                    + "    \"tripTypes\": [\"Active\", \"Nature\"],\n\n"
                    + "    \"climate\": \"Temperate\",\n\n"
                    + "    \"specialRequirements\": [\"Hiking gear required\"],\n\n"
                    + "    \"whereIs\": \"Shizuoka, Japan\",\n\n"
                    + "    \"description\": \"A thrilling journey to "
                    + "the peak of Mount Fuji...\",\n\n"
                    + "    \"whyThisPlace\": [\"UNESCO World "
                    + "Heritage site\", \"Iconic mountain\"],\n\n"
                    + "    \"imageLinks\": [\"linkToImage1.jpg\", "
                    + "\"linkToImage2.jpg\"],\n\n"
                    + "    \"mapLink\": \"linkToMap\",\n\n"
                    + "    \"likes\": 250,\n\n"
                    + "    \"comments\": [CommentDto1, "
                    + "CommentDto2]\n\n"
                    + "    \"shown\": true\n\n"
                    + "  }\n\n"
                    + "\n\n**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the card "
                    + "or user is not found by ID or email respectively.\n\n"
                    + "- `AuthorizationException` if the user "
                    + "is banned or does not have the authority "
                    + "to update the card."
    )
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public CardWithoutDistanceDto updateById(@PathVariable Long id, Authentication authentication, @Valid @RequestBody CreateCardRequestDto requestDto) {
        return cardService.updateById(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Post a Like on a Card",
            description = "Allows a user with 'USER' "
                    + "authority to like a card. "
                    + "The card's like count is incremented, "
                    + "and the card is added to the user's "
                    + "'Liked cards' collection. "
                    + "\n\n\n\n**Request:**\n\n"
                    + "PUT /post-like/{id}\n\n"
                    + "Path Variable: id (Long) - The unique "
                    + "identifier of the card to like.\n\n"
                    + "\n\n**Response:**\n\n"
                    + "This endpoint does not return a response "
                    + "body. On successful execution, it will "
                    + "result in an HTTP 200 OK status. "
                    + "\n\n\n\n**Roles with Access:**\n\n"
                    + "- 'USER'\n\n"
                    + "\n\n**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the card "
                    + "with the given ID is not found."
    )
    @PutMapping("/post-like/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void postLike(@PathVariable Long id, Authentication authentication) {
        cardService.postLike(id, authentication.getName());
    }

    @Operation(
            summary = "Remove a Like from a Card",
            description = "Permits a user with 'USER' "
                    + "authority to remove a like from a card. "
                    + "The card's like count is decremented, "
                    + "and the card is removed from the user's "
                    + "'Liked cards' collection. "
                    + "\n\n\n\n**Request:**\n\n"
                    + "PUT /remove-like/{id}\n\n"
                    + "Path Variable: id (Long) - The unique "
                    + "identifier of the card from which the "
                    + "like is to be removed.\n\n"
                    + "\n\n**Response:**\n\n"
                    + "This endpoint does not return a response "
                    + "body. Upon successful execution, it will "
                    + "result in an HTTP 200 OK status. "
                    + "\n\n\n\n**Roles with Access:**\n\n"
                    + "- 'USER'\n\n"
                    + "\n\n**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the card "
                    + "with the given ID is not found or is not "
                    + "present in the user's 'Liked cards' collection."
    )
    @PutMapping("/remove-like/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void removeLike(@PathVariable Long id, Authentication authentication) {
        cardService.removeLike(id, authentication.getName());
    }

    @Operation(
            summary = "Report a Card",
            description = "Allows a user with 'USER' "
                    + "authority to report a card. The "
                    + "user must provide the card's link "
                    + "and a text description of the issue. "
                    + "Upon successful reporting, the card's "
                    + "report count is incremented, and an "
                    + "email notification is sent.\n\n\n\n"
                    + "**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"cardLink\": \"http://example.com/card/42\",\n\n"
                    + "  \"text\": \"This card contains "
                    + "inappropriate content.\"\n\n"
                    + "}\n\n\n\n"
                    + "**Roles with Access:**\n\n"
                    + "- USER\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the "
                    + "card with the given ID does not exist.\n\n"
                    + "- `EmailServiceException` if there "
                    + "is an issue sending the email notification."
    )
    @PutMapping("/report/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void report(@PathVariable Long id, Authentication authentication, @Valid @RequestBody ReportCardRequestDto requestDto) {
        cardService.report(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Hide a Card",
            description = "Enables an admin to "
                    + "hide a card from being displayed. "
                    + "This operation does not require a "
                    + "request body. When invoked, the "
                    + "card with the specified ID will "
                    + "no longer be shown.\n\n\n\n"
                    + "**Roles with Access:**\n\n"
                    + "- ADMIN (only users with 'ADMIN' "
                    + "authority can access this endpoint)\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if "
                    + "the card with the given ID does "
                    + "not exist, indicating that no card "
                    + "could be found with the provided ID."
    )
    @PutMapping("/hide-card/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void hideCard(@PathVariable Long id) {
        cardService.hideCard(id);
    }

    @Operation(
            summary = "Reveal a Hidden Card",
            description = "This endpoint allows "
                    + "an administrator to make a "
                    + "previously hidden card visible "
                    + "again. The card to be revealed "
                    + "is identified by its unique ID "
                    + "in the path variable.\n\n\n\n"
                    + "**Roles with Access:**\n\n"
                    + "- ADMIN (only users with 'ADMIN' "
                    + "authority can perform this action)\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if "
                    + "no card is found with the provided ID, "
                    + "indicating that the card either does "
                    + "not exist or has been removed."
    )
    @PutMapping("/reveal-card/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void revealCard(@PathVariable Long id) {
        cardService.revealCard(id);
    }

    @Operation(
            summary = "Delete a Card",
            description = "This endpoint allows a " 
                    + "user with 'USER' authority to " 
                    + "delete a card. The card is " 
                    + "identified by its ID, which is " 
                    + "passed as a path variable. The " 
                    + "operation is permitted if the user " 
                    + "has more than one authority or if " 
                    + "the user is the author of the card.\n\n\n\n"
                    + "**Roles with Access:**\n\n"
                    + "- USER (with conditions specified " 
                    + "in the method's logic)\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if the " 
                    + "card with the given ID or the user with " 
                    + "the given email does not exist.\n\n"
                    + "- `AuthorizationException` if the " 
                    + "authenticated user does not have the " 
                    + "required authority to delete the card."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteCard(@PathVariable Long id, Authentication authentication) {
        cardService.deleteById(id, authentication.getName());
    }
}
