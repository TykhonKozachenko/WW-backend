package wander.wise.application.exception.custom;

public class ImageSearchException extends RuntimeException {
    public ImageSearchException(String message, Exception exception) {
        super(message, exception);
    }
}
