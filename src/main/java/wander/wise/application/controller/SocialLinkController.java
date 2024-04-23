package wander.wise.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import wander.wise.application.dto.social.link.CreateSocialLinkRequestDto;
import wander.wise.application.dto.social.link.SocialLinkDto;
import wander.wise.application.service.social.link.SocialLinkService;

@Tag(name = "Social links management endpoints")
@RestController
@RequestMapping("/social-links")
@RequiredArgsConstructor
public class SocialLinkController {
    private final SocialLinkService socialLinkService;

    @Operation(
            summary = "Add Social Link",
            description = "Allows an authenticated "
                    + "user with 'USER' authority to "
                    + "add a new social link. "
                    + "The social link is associated "
                    + "with the user's account based "
                    + "on the provided user ID. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"userId\": 42,\n\n"
                    + "  \"name\": \"LinkedIn\",\n\n"
                    + "  \"link\": \"https://www.linkedin"
                    + ".com/in/username\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"name\": \"LinkedIn\",\n\n"
                    + "  \"link\": \"https://www.linkedin"
                    + ".com/in/username\"\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the user ID is not found), "
                    + "AuthorizationException (if the user's "
                    + "email does not match the authenticated user)"
    )
    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public SocialLinkDto addSocialLink(Authentication authentication, @RequestBody CreateSocialLinkRequestDto requestDto) {
        return socialLinkService.save(authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Update Social Link",
            description = "Updates an existing social "
                    + "link for the authenticated user. "
                    + "The user must have 'USER' authority "
                    + "and be the owner of the social link "
                    + "to update it. "
                    + "The request body should include the "
                    + "user ID, the new name of the social "
                    + "link, and the new URL. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"userId\": 42,\n\n"
                    + "  \"name\": \"Personal Blog\",\n\n"
                    + "  \"link\": \"https://www.personalblog.com\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"name\": \"Personal Blog\",\n\n"
                    + "  \"link\": \"https://www.personalblog.com\"\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the social link or user is not found), "
                    + "AuthorizationException (if the user is not "
                    + "authorized to update the social link)"
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public SocialLinkDto updateSocialLink(@PathVariable Long id, Authentication authentication, @RequestBody CreateSocialLinkRequestDto requestDto) {
        return socialLinkService.updateSocialLinkById(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Delete Social Link",
            description = "Deletes a social link by " 
                    + "its ID. The operation can only be " 
                    + "performed by the user who owns the social link. " 
                    + "The endpoint does not require a request body. " 
                    + "\n\n\n\n**Path Variable Example:**\n\n" 
                    + "id: 123\n\n\n\n" 
                    + "Roles with access: USER\n\n\n\n" 
                    + "Possible exceptions: EntityNotFoundException " 
                    + "(if the social link with the given ID is not found), " 
                    + "AuthorizationException (if the authenticated " 
                    + "user is not the owner of the social link)"
    )
    @DeleteMapping("/{id}")
    public void deleteSocialLink(@PathVariable Long id, Authentication authentication) {
        socialLinkService.deleteSocialLinkById(id, authentication.getName());
    }
}
