package wander.wise.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import wander.wise.application.dto.comment.CommentDto;
import wander.wise.application.dto.comment.CreateCommentRequestDto;
import wander.wise.application.dto.comment.ReportCommentRequestDto;
import wander.wise.application.service.comment.CommentService;

@Tag(name = "Comment management endpoints")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @Operation(
            summary = "Create a New Comment",
            description = "Allows an authenticated user "
                    + "with 'USER' authority to post a "
                    + "new comment on a card. "
                    + "The comment includes text and a star rating. "
                    + "Banned users are prohibited from posting comments. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"cardId\": 123,\n\n"
                    + "  \"text\": \"Great card! Helped me "
                    + "to find excellent trip.\",\n\n"
                    + "  \"stars\": 5\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 456,\n\n"
                    + "  \"author\": \"JohnDoe92\",\n\n"
                    + "  \"timeStamp\": \"2024-04-23T08:45:30\",\n\n"
                    + "  \"text\": \"Great card! Helped "
                    + "me to find excellent trip.\",\n\n"
                    + "  \"stars\": 5\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the user's email is not found), "
                    + "AuthorizationException (if the user is "
                    + "banned and tries to post a comment)"
    )
    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public CommentDto save(Authentication authentication, @Valid @RequestBody CreateCommentRequestDto requestDto) {
        return commentService.save(authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Update Comment",
            description = "Updates an existing comment by its "
                    + "ID for the authenticated user. "
                    + "Only users with 'USER' authority "
                    + "can access this endpoint. "
                    + "The request body must contain the "
                    + "card ID, the updated text of the "
                    + "comment, and the star rating. "
                    + "\n\n\n\n**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"cardId\": 10,\n\n"
                    + "  \"text\": \"Updated comment text.\",\n\n"
                    + "  \"stars\": 4\n\n"
                    + "}\n\n\n\n"
                    + "**Response Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"id\": 20,\n\n"
                    + "  \"author\": \"User123\",\n\n"
                    + "  \"timeStamp\": \"2024-04-23T10:00:00\",\n\n"
                    + "  \"text\": \"Updated comment text.\",\n\n"
                    + "  \"stars\": 4\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the comment or user is not found), "
                    + "AuthorizationException (if the user is not "
                    + "authorized to update the comment)"
    )
    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('USER')")
    public CommentDto update(@PathVariable Long id, Authentication authentication, @Valid @RequestBody CreateCommentRequestDto requestDto) {
        return commentService.update(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Report a Comment",
            description = "Allows an authenticated user "
                    + "with 'USER' authority to report a comment. "
                    + "The report includes the author of the "
                    + "comment, the comment text, and the report text. "
                    + "Each report increments the comment's "
                    + "report count and sends an email "
                    + "notification to the admin. "
                    + "\n\n\n\n**Path Variable Example:**\n\n"
                    + "id: 789\n\n\n\n"
                    + "**Request Body Example:**\n\n"
                    + "{\n\n"
                    + "  \"commentAuthor\": \"User456\",\n\n"
                    + "  \"commentText\": \"This is the "
                    + "comment text.\",\n\n"
                    + "  \"reportText\": \"This comment is "
                    + "inappropriate because...\"\n\n"
                    + "}\n\n\n\n"
                    + "Roles with access: USER\n\n\n\n"
                    + "Possible exceptions: EntityNotFoundException "
                    + "(if the comment with the given ID is not found)"
    )
    @PutMapping("/report/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void report(@PathVariable Long id, Authentication authentication, @Valid @RequestBody ReportCommentRequestDto requestDto) {
        commentService.report(id, authentication.getName(), requestDto);
    }

    @Operation(
            summary = "Delete Comment by ID",
            description = "Deletes a comment based " 
                    + "on the provided ID. " 
                    + "This operation can only be " 
                    + "performed by authenticated users " 
                    + "with 'USER' authority. " 
                    + "Users can delete their own comments " 
                    + "or, if they have more than one authority, " 
                    + "comments of other users. " 
                    + "\n\n\n\n**Path Variable Example:**\n\n" 
                    + "id: 12345\n\n\n\n" 
                    + "Roles with access: USER\n\n\n\n" 
                    + "Possible exceptions: EntityNotFoundException " 
                    + "(if the comment or user is not found), " 
                    + "AuthorizationException (if the user does " 
                    + "not have permission to delete the comment: " 
                    + "user should be the author of the comment " 
                    + "or has multiple roles)"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteComment(@PathVariable Long id, Authentication authentication) {
        commentService.deleteById(id, authentication.getName());
    }
}
