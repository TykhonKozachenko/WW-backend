package wander.wise.application.service.comment;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wander.wise.application.dto.comment.CommentDto;
import wander.wise.application.dto.comment.CreateCommentRequestDto;
import wander.wise.application.dto.comment.ReportCommentRequestDto;
import wander.wise.application.exception.custom.AuthorizationException;
import wander.wise.application.mapper.CommentMapper;
import wander.wise.application.model.Comment;
import wander.wise.application.model.User;
import wander.wise.application.repository.comment.CommentRepository;
import wander.wise.application.repository.user.UserRepository;
import wander.wise.application.service.api.email.EmailService;
import wander.wise.application.service.user.UserService;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public CommentDto save(String email, CreateCommentRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));
        if (!user.isBanned()) {
            Comment newComment = commentMapper.toModel(requestDto);
            newComment.setTimeStamp(LocalDateTime.now());
            newComment.setUser(user);
            return commentMapper.toDto(commentRepository.save(newComment));
        } else {
            throw new AuthorizationException("Access denied. User is banned.");
        }
    }

    @Override
    public CommentDto update(Long id, String email, CreateCommentRequestDto requestDto) {
        Comment updatedComment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find comment by id: " + id));
        userService.findUserAndAuthorize(updatedComment.getUser().getId(), email);
        updatedComment = commentMapper.updateCommentFromDto(updatedComment, requestDto);
        return commentMapper.toDto(commentRepository.save(updatedComment));
    }

    @Override
    public void report(Long id, String email, ReportCommentRequestDto requestDto) {
        Comment reportedComment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find comment by id: " + id));
        reportedComment.setReports(reportedComment.getReports() + 1);
        String message = new StringBuilder()
                .append("User email: ").append(email)
                .append(System.lineSeparator())
                .append("Comment author: ").append(requestDto.commentAuthor())
                .append(System.lineSeparator())
                .append("Comment text: ").append(requestDto.commentText())
                .append(System.lineSeparator())
                .append("Report text: ").append(requestDto.reportText())
                .append(System.lineSeparator())
                .append("Comment was reported: ")
                .append(reportedComment.getReports()).append(" times")
                .toString();
        emailService.sendEmail(
                "budzetbudzet4@gmail.com",
                "Report for comment",
                message);
        commentRepository.save(reportedComment);
    }

    @Override
    public void deleteById(Long id, String email) {
        Comment deletedComment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find comment by id: " + id));
        User deletingUser = userRepository.findById(deletedComment.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by id: " + id));
        if (deletingUser.getAuthorities().size() > 1
                || deletingUser.getEmail().equals(email)) {
            userService.findUserAndAuthorize(deletedComment.getUser().getId(), email);
            commentRepository.deleteById(id);
        } else {
            throw new AuthorizationException("Access denied. You can't "
                    + "delete comments of this user");
        }
    }
}
