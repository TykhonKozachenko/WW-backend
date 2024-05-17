package wander.wise.application.exception;

import jakarta.persistence.EntityNotFoundException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import wander.wise.application.exception.custom.AiException;
import wander.wise.application.exception.custom.AuthorizationException;
import wander.wise.application.exception.custom.CardSearchException;
import wander.wise.application.exception.custom.EmailServiceException;
import wander.wise.application.exception.custom.ImageSearchException;
import wander.wise.application.exception.custom.JwtValidationException;
import wander.wise.application.exception.custom.MapsException;
import wander.wise.application.exception.custom.RegistrationException;
import wander.wise.application.exception.custom.StorageException;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        List<String> errors = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(this::getErrorMessage)
                .toList();
        body.put("errors", errors);
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AiException.class)
    public ResponseEntity<String> handleAiException(AiException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<String> handleAuthorizationException(AuthorizationException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CardSearchException.class)
    public ResponseEntity<String> handleCardSearchException(CardSearchException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailServiceException.class)
    public ResponseEntity<String> handleEmailServiceException(EmailServiceException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ImageSearchException.class)
    public ResponseEntity<String> handleImageSearchException(ImageSearchException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<String> handleJwtValidationException(JwtValidationException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MapsException.class)
    public ResponseEntity<String> handleMapsException(MapsException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<String> handleRegistrationException(RegistrationException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<String> handleSqlIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<String> handleStorageException(StorageException exception) {
        return new ResponseEntity<>(getResponseMessage(exception),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getErrorMessage(ObjectError error) {
        if (error instanceof FieldError) {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            return field + ": " + message;
        }
        return error.getDefaultMessage();
    }

    private static String getResponseMessage(Exception exception) {
        String exceptionName = exception.getClass().getName();
        StringBuilder builder = new StringBuilder();
        builder.append(exceptionName.substring(exceptionName.lastIndexOf(".") + 1))
                .append(": ")
                .append(exception.getMessage());
        if (exception.getCause() != null) {
            Throwable causeException = exception.getCause();
            String causeExceptionName = causeException.getClass().getName();
            builder.append(System.lineSeparator())
                    .append("Caused by ")
                    .append(causeExceptionName.substring(causeExceptionName.lastIndexOf(".") + 1))
                    .append(": ")
                    .append(causeException.getMessage());
        }
        return builder.toString();
    }
}
