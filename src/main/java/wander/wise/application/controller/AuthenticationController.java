package wander.wise.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import wander.wise.application.dto.user.login.LoginRequestDto;
import wander.wise.application.dto.user.login.LoginResponseDto;
import wander.wise.application.dto.user.registration.ConfirmEmailRequestDto;
import wander.wise.application.dto.user.registration.RegisterUserRequestDto;
import wander.wise.application.dto.user.UserDto;
import wander.wise.application.dto.user.update.RestorePasswordRequestDto;
import wander.wise.application.security.AuthenticationService;
import wander.wise.application.service.user.UserService;

@Tag(name = "Authentication endpoints")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user",
            description = "This endpoint registers a new user "
                    + "with the provided email, password, "
                    + "and repeat password. "
                    + "Upon successful registration, it returns "
                    + "the user's details, including a unique "
                    + "email confirmation code. "
                    + "The request body must contain a valid "
                    + "email and matching passwords of at "
                    + "least 8 characters.\n\n Each user has 3 "
                    + "default collections (\"Liked cards\", "
                    + "\"Created cards\", \"Saved cards\")"
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"email\": \"user@example.com\",\n\n"
                    + "  \"password\": \"password123\",\n\n"
                    + "  \"repeatPassword\": \"password123\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 1,\n\n"
                    + "  \"pseudonym\": \"johndoe\",\n\n"
                    + "  \"email\": \"user@example.com\",\n\n"
                    + "  \"firstName\": \"John\",\n\n"
                    + "  \"lastName\": \"Doe\",\n\n"
                    + "  \"profileImage\": \"link\",\n\n"
                    + "  \"location\": \"New York\",\n\n"
                    + "  \"bio\": \"Developer\",\n\n"
                    + "  \"roleIds\": [1 (ROOT), 2 (ADMIN), 3 (USER)],\n\n"
                    + "  \"emailConfirmCode\": \"1234\"\n\n"
                    + "}\n\n\n\n"
                    + "**Roles Access:** All users can access "
                    + "this endpoint to register a new account.\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `RegistrationException` if an account "
                    + "with the provided email already exists.\n\n"
                    + "- `MethodArgumentNotValidException` if "
                    + "the provided passwords do not match or do "
                    + "not meet the size requirements.\n\n\n\n"
                    + "- `EmailServiceException` if there "
                    + "is an issue sending the email with "
                    + "the confirmation code.\n\n\n\n"
                    + "**Field Validation:**\n\n"
                    + "- `email`: Must be a well-formed email address.\n\n"
                    + "- `password` & `repeatPassword`: Must "
                    + "not be blank, must be at least 8 characters long.\n\n"
                    + "**PAY ATTENTION:**\n\n"
                    + "Each new user is banned and can't create "
                    + "cards or comments. To became unban user "
                    + "should confirm email."
    )
    @PostMapping("/register")
    public UserDto registerNewUser(@Valid @RequestBody RegisterUserRequestDto requestDto) {
        return userService.save(requestDto);
    }

    @Operation(
            summary = "Authenticate user and provide a token",
            description = "This endpoint authenticates "
                    + "a user with their email and password. "
                    + "If authentication is successful, "
                    + "it returns a JWT token that can be "
                    + "used for accessing protected routes. "
                    + "The request body must contain a valid"
                    + " email and a password of at least 8 characters. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"email\": \"user@example.com\",\n\n"
                    + "  \"password\": \"strongpassword\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n\n"
                    + "}\n\n\n\n"
                    + "**Roles Access:** This endpoint can "
                    + "be accessed by users who are not currently "
                    + "authenticated.\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `BadCredentialsException` if the "
                    + "credentials provided are incorrect."
    )
    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }

    @Operation(
            summary = "Refresh JWT token",
            description = "This endpoint is used to "
                    + "refresh the JWT token for authenticated users. "
                    + "It takes the current authentication "
                    + "context and issues a new JWT token. "
                    + "There is no request body for this "
                    + "endpoint as it uses the existing authentication context. "
                    + "\n\n\n\n**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n\n"
                    + "}\n\n\n\n"
                    + "**Roles Access:** This endpoint can "
                    + "only be accessed by users who are already "
                    + "authenticated and possess a valid JWT token.\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `AuthenticationException` if the "
                    + "authentication information is not valid "
                    + "or has expired."
    )
    @GetMapping("/refresh-jwt")
    public LoginResponseDto refreshJwt(Authentication authentication) {
        return authenticationService.refreshJwt(authentication.getName());
    }

    @Operation(
            summary = "Confirm user email",
            description = "This endpoint confirms "
                    + "the user's email address. "
                    + "Upon receiving a valid email, "
                    + "it updates the user's status to not "
                    + "banned and issues a new JWT token. "
                    + "The request body must contain a "
                    + "valid email address. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"email\": \"user@example.com\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"token\": \"newJwtToken12345\"\n\n"
                    + "}\n\n\n\n"
                    + "**Roles Access:** This endpoint "
                    + "can be accessed by users who have "
                    + "received an email confirmation code "
                    + "and are in the process of confirming "
                    + "their email address.\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `UsernameNotFoundException` if "
                    + "no account is associated with the "
                    + "provided email address."
    )
    @PostMapping("/confirm-email")
    public LoginResponseDto confirmEmail(@Valid @RequestBody ConfirmEmailRequestDto requestDto) {
        return userService.confirmEmail(requestDto.email());
    }
    
    @Operation(
            summary = "Restore forgotten password",
            description = "This endpoint assists " 
                    + "users in restoring access to " 
                    + "their account when they have " 
                    + "forgotten their password. "
                    + "It generates a new random " 
                    + "password, encodes it, and updates " 
                    + "the user's record in the database. "
                    + "An email is then sent to the " 
                    + "user with the new password. The " 
                    + "request body must contain a valid email address. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"email\": \"user@example.com\"\n\n"
                    + "}\n\n\n\n"
                    + "**Response:** This endpoint does " 
                    + "not return a response body as it " 
                    + "triggers an email service that sends " 
                    + "the new password directly to the user's " 
                    + "email address.\n\n\n\n"
                    + "**Roles Access:** This endpoint can " 
                    + "be accessed by any user who needs to " 
                    + "restore their password.\n\n\n\n"
                    + "**Possible Exceptions:**\n\n"
                    + "- `EntityNotFoundException` if no " 
                    + "account is associated with the provided " 
                    + "email address.\n\n"
                    + "- `EmailServiceException` if there " 
                    + "is an issue sending the email with " 
                    + "the new password."
    )
    @PostMapping("restore-password")
    public void restorePassword(@Valid @RequestBody RestorePasswordRequestDto requestDto) {
        userService.restorePassword(requestDto);
    }
}
