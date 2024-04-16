package wander.wise.application.exception;

public class ImageSearchException extends RuntimeException {
    public ImageSearchException(String message, Exception exception) {
        super(message, exception);
    }
}
