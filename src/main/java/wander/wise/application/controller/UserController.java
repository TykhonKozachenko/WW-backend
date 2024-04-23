package wander.wise.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import wander.wise.application.dto.collection.CollectionWithoutCardsDto;
import wander.wise.application.dto.social.link.SocialLinkDto;
import wander.wise.application.dto.user.login.LoginResponseDto;
import wander.wise.application.dto.user.update.UpdateUserInfoRequestDto;
import wander.wise.application.dto.user.UserDto;
import wander.wise.application.dto.user.update.*;
import wander.wise.application.service.user.UserService;
import java.util.Set;

@Tag(name = "User management endpoints")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Get User Profile",
            description = "Retrieves the profile "
                    + "information of a user by their "
                    + "unique identifier (ID). "
                    + "This endpoint is publicly accessible "
                    + "and does not require any specific "
                    + "user authority. "
                    + "\n\n\n\n**Path Variable Example:**\n\n"
                    + "id: 123\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 123,\n\n"
                    + "  \"pseudonym\": \"CoolUser123\",\n\n"
                    + "  \"email\": \"cooluser123@example.com\",\n\n"
                    + "  \"firstName\": \"Cool\",\n\n"
                    + "  \"lastName\": \"User\",\n\n"
                    + "  \"profileImage\": \"http://example."
                    + "com/profile.jpg\",\n\n"
                    + "  \"location\": \"New York, USA\",\n\n"
                    + "  \"bio\": \"Passionate developer and "
                    + "tech enthusiast.\",\n\n"
                    + "  \"roleIds\": [1, 2],\n\n"
                    + "  \"emailConfirmCode\": \"\"\n\n"
                    + "}\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if no user with the given ID is found)"
    )
    @GetMapping("/{id}/profile")
    public UserDto getUserProfile(@PathVariable Long id) {
        return userService.findById(id);
    }

    @Operation(
            summary = "Get User Social Links",
            description = "Retrieves a set of social "
                    + "link DTOs for a user by their "
                    + "unique identifier (ID). "
                    + "This endpoint is publicly accessible "
                    + "and does not require any specific "
                    + "user authority. "
                    + "\n\n\n\n**Path Variable Example:**\n\n"
                    + "id: 123\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "[\n\n"
                    + "  {\n\n"
                    + "    \"name\": \"LinkedIn\",\n\n"
                    + "    \"link\": \"https://www.linkedin."
                    + "com/in/username\"\n\n"
                    + "  },\n\n"
                    + "  {\n\n"
                    + "    \"name\": \"Twitter\",\n\n"
                    + "    \"link\": \"https://twitter.com/"
                    + "username\"\n\n"
                    + "  }\n\n"
                    + "]\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if no user with the given ID is found)"
    )
    @GetMapping("/{id}/social-links")
    public Set<SocialLinkDto> getUserSocialLinks(@PathVariable Long id) {
        return userService.getUserSocialLinks(id);
    }

    @Operation(
            summary = "Get User Collections",
            description = "Retrieves a set of collections "
                    + "without card details for a user by "
                    + "their unique identifier (ID). "
                    + "Accessible only by authenticated users "
                    + "with 'USER' authority, this endpoint "
                    + "ensures that the requesting user is "
                    + "authorized to view the collections. "
                    + "Collections are returned with initialized "
                    + "image links if not already present. "
                    + "\n\n\n\n**Path Variable Example:**\n\n"
                    + "id: 123\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "[\n\n"
                    + "  {\n\n"
                    + "    \"id\": 1,\n\n"
                    + "    \"author\": \"AuthorName\",\n\n"
                    + "    \"name\": \"CollectionName\",\n\n"
                    + "    \"imageLink\": \"http://example."
                    + "com/image.jpg\",\n\n"
                    + "    \"isPublic\": true\n\n"
                    + "  }\n\n"
                    + "  // ... other collections\n\n"
                    + "]\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the user with the given ID is not found), "
                    + "AuthorizationException (if the authenticated "
                    + "user is not authorized to view the collections)"
    )
    @GetMapping("/{id}/collections")
    @PreAuthorize("hasAuthority('USER')")
    public Set<CollectionWithoutCardsDto> getUserCollections(@PathVariable Long id, Authentication authentication) {
        return userService.getUserCollections(id, authentication.getName());
    }

    @Operation(
            summary = "Update User Information",
            description = "Allows an authenticated "
                    + "user with 'USER' authority to "
                    + "update their profile information. "
                    + "The endpoint requires the user "
                    + "ID in the path variable and accepts "
                    + "a request body with the new user information. "
                    + "It checks if the pseudonym is already "
                    + "in use and updates the user's information "
                    + "if it's not. "
                    + "\n\n\n\n**Path Variable Example:**\n\n"
                    + "id: 42\n\n\n\n"
                    + "**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"pseudonym\": \"NewPseudonym\",\n\n"
                    + "  \"firstName\": \"John\",\n\n"
                    + "  \"lastName\": \"Doe\",\n\n"
                    + "  \"profileImage\": \"http://example."
                    + "com/profile.jpg\",\n\n"
                    + "  \"location\": \"New York, USA\",\n\n"
                    + "  \"bio\": \"Developer and tech "
                    + "enthusiast.\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 42,\n\n"
                    + "  \"pseudonym\": \"NewPseudonym\",\n\n"
                    + "  \"email\": \"johndoe@example.com\",\n\n"
                    + "  \"firstName\": \"John\",\n\n"
                    + "  \"lastName\": \"Doe\",\n\n"
                    + "  \"profileImage\": \"http://example."
                    + "com/profile.jpg\",\n\n"
                    + "  \"location\": \"New York, USA\",\n\n"
                    + "  \"bio\": \"Developer and tech enthusiast.\",\n\n"
                    + "  \"roleIds\": [1, 2, 3],\n\n"
                    + "  \"emailConfirmCode\": \"\"\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the user with the given ID is not found), "
                    + "AuthorizationException (if the authenticated "
                    + "user's email does not match the email "
                    + "associated with the user ID), "
                    + "RegistrationException (if the pseudonym "
                    + "is already in use)"
    )
    @PutMapping("/update-user-info/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public UserDto updateUserInfo(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateUserInfoRequestDto requestDto) {
        return userService.updateUserInfo(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Request Update User Email",
            description = "Allows an authenticated user "
                    + "with 'USER' authority to request an "
                    + "update to their email address. "
                    + "The user must provide the new email "
                    + "address in the request body. "
                    + "A confirmation code is sent to the "
                    + "new email address, which the user "
                    + "must use to confirm the update. "
                    + "\n\n\n\n**Path Variable Example:**\n\n"
                    + "id: 42\n\n\n\n"
                    + "**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"email\": \"newemail@example.com\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 42,\n\n"
                    + "  \"pseudonym\": \"UserPseudonym\",\n\n"
                    + "  \"email\": \"newemail@example.com\",\n\n"
                    + "  \"firstName\": \"John\",\n\n"
                    + "  \"lastName\": \"Doe\",\n\n"
                    + "  \"profileImage\": \"http://example."
                    + "com/profile.jpg\",\n\n"
                    + "  \"location\": \"New York, USA\",\n\n"
                    + "  \"bio\": \"Developer and tech enthusiast.\",\n\n"
                    + "  \"roleIds\": [1],\n\n"
                    + "  \"emailConfirmCode\": \"1234\"\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the user with the given ID is not found), "
                    + "AuthorizationException (if the authenticated "
                    + "user's email does not match the email "
                    + "associated with the user ID), "
                    + "EmailServiceException (if there is "
                    + "an issue sending the confirmation "
                    + "code to the new email address)"
    )
    @PostMapping("/request-update-user-email/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public UserDto requestUpdateUserEmail(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateUserEmailRequestDto requestDto) {
        return userService.requestUpdateUserEmail(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Update User Email",
            description = "Allows an authenticated "
                    + "user with 'USER' authority to "
                    + "update their email address. "
                    + "The user must provide the new "
                    + "email address in the request body. "
                    + "Upon successful update, a new "
                    + "JWT token is generated and returned. "
                    + "\n\n\n\n**Path Variable Example:**\n\n"
                    + "id: 42\n\n\n\n"
                    + "**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"email\": \"newemail@example.com\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"token\": \"eyJhbGciOiJIUzI1NiIs"
                    + "InR5cCI6IkpXVCJ9...\"\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the user with the given ID is not found), "
                    + "AuthorizationException (if the "
                    + "authenticated user's email does not "
                    + "match the email associated with the user ID)"
    )
    @PutMapping("/update-user-email/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public LoginResponseDto updateUserEmail(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateUserEmailRequestDto requestDto) {
        return userService.updateUserEmail(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Request Update User Password",
            description = "Initiates a password update "
                    + "request for an authenticated user "
                    + "with 'USER' authority. "
                    + "The user is identified by their "
                    + "unique ID and must be authenticated. "
                    + "A confirmation code is sent to the "
                    + "user's email address to proceed "
                    + "with the password update. "
                    + "\n\n\n\n**Path Variable Example:**\n\n"
                    + "id: 42\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 42,\n\n"
                    + "  \"pseudonym\": \"UserPseudonym\",\n\n"
                    + "  \"email\": \"user@example.com\",\n\n"
                    + "  \"firstName\": \"John\",\n\n"
                    + "  \"lastName\": \"Doe\",\n\n"
                    + "  \"profileImage\": \"http://example."
                    + "com/profile.jpg\",\n\n"
                    + "  \"location\": \"New York, USA\",\n\n"
                    + "  \"bio\": \"Developer and tech enthusiast.\",\n\n"
                    + "  \"roleIds\": [1],\n\n"
                    + "  \"emailConfirmCode\": \"1234\"\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the user with the given ID is not found), "
                    + "AuthorizationException (if the authenticated "
                    + "user's email does not match the email "
                    + "associated with the user ID), "
                    + "EmailServiceException (if there is an "
                    + "issue sending the confirmation code to "
                    + "the user's email address)"
    )
    @PostMapping("/request-update-user-password/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public UserDto requestUpdateUserPassword(
            @PathVariable Long id,
            Authentication authentication) {
        return userService.requestUpdateUserPassword(id, authentication.getName());
    }

    @Operation(
            summary = "Request Update User Password",
            description = "Allows a user with 'USER' "
                    + "authority to request a password "
                    + "update. Upon successful authorization, "
                    + "an email confirmation code is sent "
                    + "to the user's email address. The "
                    + "response includes the user's details "
                    + "along with the sent confirmation code.\n\n\n\n"
                    + "**Request Body Example:**\n\n"
                    + "```json\n\n"
                    + "{\n\n"
                    + "  \"password\": \"newPassword123\",\n\n"
                    + "  \"repeatPassword\": \"newPassword123\"\n\n"
                    + "}\n\n"
                    + "```\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "```json\n\n"
                    + "{\n\n"
                    + "  \"id\": 1,\n\n"
                    + "  \"pseudonym\": \"johndoe\",\n\n"
                    + "  \"email\": \"john.doe@example.com\",\n\n"
                    + "  \"firstName\": \"John\",\n\n"
                    + "  \"lastName\": \"Doe\",\n\n"
                    + "  \"profileImage\": \"http://example"
                    + ".com/image.jpg\",\n\n"
                    + "  \"location\": \"New York\",\n\n"
                    + "  \"bio\": \"Developer\",\n\n"
                    + "  \"roleIds\": [2, 3],\n\n"
                    + "  \"emailConfirmCode\": \"123456\"\n\n"
                    + "}\n\n"
                    + "```\n\n\n\n"
                    + "**Roles with Access:**\n\n"
                    + "- `USER`\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` - if the "
                    + "user with the given ID is not found.\n\n"
                    + "- `AuthorizationException` - if the "
                    + "email provided does not match the "
                    + "email associated with the user ID.\n\n"
                    + "- `EmailServiceException` - if there "
                    + "is an issue sending the email with "
                    + "the confirmation code."
    )
    @PutMapping("/update-user-password/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public LoginResponseDto updateUserPassword(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateUserPasswordRequestDto requestDto) {
        return userService.updateUserPassword(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Update User Roles",
            description = "This endpoint is used to "
                    + "update the roles of a specific "
                    + "user. Only users with 'ROOT' "
                    + "authority can access this endpoint. "
                    + "The request must include a set of "
                    + "role IDs that will replace the user's "
                    + "current roles.\n\n\n\n"
                    + "**Request Body Example:**\n\n"
                    + "```json\n\n"
                    + "{\n\n"
                    + "  \"roleIds\": [1, 2, 3]\n\n"
                    + "}\n\n"
                    + "```\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "```json\n\n"
                    + "{\n\n"
                    + "  \"id\": 1,\n\n"
                    + "  \"pseudonym\": \"johndoe\",\n\n"
                    + "  \"email\": \"john.doe@example.com\",\n\n"
                    + "  \"firstName\": \"John\",\n\n"
                    + "  \"lastName\": \"Doe\",\n\n"
                    + "  \"profileImage\": \"http://example."
                    + "com/image.jpg\",\n\n"
                    + "  \"location\": \"New York\",\n\n"
                    + "  \"bio\": \"Developer\",\n\n"
                    + "  \"roleIds\": [1, 2, 3],\n\n"
                    + "  \"emailConfirmCode\": \"\"\n\n"
                    + "}\n\n"
                    + "```\n\n\n\n"
                    + "**Roles with Access:**\n\n"
                    + "- `ROOT`\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` - if "
                    + "no user is found with the given ID.\n\n"
                    + "- `MethodArgumentNotValidException` - "
                    + "if the request body does not meet the "
                    + "validation requirements (e.g., `roleIds` is empty)."
    )
    @PutMapping("/update-user-roles/{id}")
    @PreAuthorize("hasAuthority('ROOT')")
    public UserDto updateUserRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequestDto requestDto) {
        return userService.updateUserRoles(id, requestDto);
    }

    @Operation(
            summary = "Delete User",
            description = "This endpoint allows a user "
                    + "with 'USER' authority to delete their "
                    + "own account. The user must be "
                    + "authenticated and the email associated "
                    + "with the authentication must match "
                    + "the email of the user being deleted.\n\n\n\n"
                    + "**No Request Body Required**\n\n\n\n"
                    + "**No Response Body** as the operation "
                    + "does not return any content upon "
                    + "successful deletion.\n\n\n\n"
                    + "**Roles with Access:**\n\n"
                    + "- `USER` - Users can only delete "
                    + "their own account.\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` - if "
                    + "no user is found with the given ID.\n\n"
                    + "- `AuthorizationException` - if the "
                    + "authenticated user's email does "
                    + "not match the email of the user with the given ID."
    )
    @DeleteMapping("/delete-user/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteUser(@PathVariable Long id, Authentication authentication) {
        userService.deleteUser(id, authentication.getName());
    }

    @Operation(
            summary = "Ban User",
            description = "This endpoint is responsible "
                    + "for banning a user. It can only be "
                    + "accessed by users with 'ADMIN' "
                    + "authority. When a user is banned, "
                    + "their account is marked as such, and "
                    + "they may lose access to certain "
                    + "functionalities within the application.\n\n\n\n"
                    + "**Request:**\n\n"
                    + "No request body is required. The user "
                    + "ID is provided in the path variable.\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "```json\n\n"
                    + "{\n\n"
                    + "  \"id\": 1,\n\n"
                    + "  \"pseudonym\": \"johndoe\",\n\n"
                    + "  \"email\": \"john.doe@example.com\",\n\n"
                    + "  \"firstName\": \"John\",\n\n"
                    + "  \"lastName\": \"Doe\",\n\n"
                    + "  \"profileImage\": \"http://example.com/"
                    + "image.jpg\",\n\n"
                    + "  \"location\": \"New York\",\n\n"
                    + "  \"bio\": \"Developer\",\n\n"
                    + "  \"roleIds\": [1, 2, 3],\n\n"
                    + "  \"emailConfirmCode\": \"\",\n\n"
                    + "  \"banned\": true\n\n"
                    + "}\n\n"
                    + "```\n\n\n\n"
                    + "**Roles with Access:**\n\n"
                    + "- `ADMIN`\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` - if no "
                    + "user is found with the given ID."
    )
    @PutMapping("/ban-user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDto banUser(@PathVariable Long id) {
        return userService.banUser(id);
    }

    @Operation(
            summary = "Unban User",
            description = "This endpoint allows " 
                    + "administrators to remove the " 
                    + "ban from a user's account. It " 
                    + "is accessible only by users with " 
                    + "'ADMIN' authority. The endpoint " 
                    + "takes the user ID as a path variable " 
                    + "and, if the user is found and currently" 
                    + " banned, the ban is lifted.\n\n\n\n" 
                    + "**Request:**\n\n" 
                    + "No request body is required. The user " 
                    + "ID is provided in the path variable.\n\n\n\n" 
                    + "**Response Body Example:**\n\n" 
                    + "```json\n\n" 
                    + "{\n\n" 
                    + "  \"id\": 1,\n\n" 
                    + "  \"pseudonym\": \"johndoe\",\n\n" 
                    + "  \"email\": \"john.doe@example.com\",\n\n" 
                    + "  \"firstName\": \"John\",\n\n" 
                    + "  \"lastName\": \"Doe\",\n\n" 
                    + "  \"profileImage\": \"http://example.com/" 
                    + "image.jpg\",\n\n" 
                    + "  \"location\": \"New York\",\n\n" 
                    + "  \"bio\": \"Developer\",\n\n" 
                    + "  \"roleIds\": [2, 3],\n\n" 
                    + "  \"banned\": false,\n\n" 
                    + "  \"emailConfirmCode\": \"123456\"\n\n" 
                    + "}\n\n" 
                    + "```\n\n\n\n" 
                    + "**Roles with Access:**\n\n" 
                    + "- `ADMIN`\n\n\n\n" 
                    + "**Possible Exceptions:**\n\n" 
                    + "- `EntityNotFoundException` - if no " 
                    + "user is found with the given ID."
    )
    @PutMapping("/unban-user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDto unbanUser(@PathVariable Long id) {
        return userService.unbanUser(id);
    }
}
