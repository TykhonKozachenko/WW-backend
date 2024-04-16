package wander.wise.application.exception;

public class AiException extends RuntimeException {
    public AiException(String message, Exception exception) {
        super(message, exception);
    }
}
