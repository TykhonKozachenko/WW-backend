package wander.wise.application.exception.custom;

public class MapsException extends RuntimeException {
    public MapsException(String message, Exception exception) {
        super(message, exception);
    }
}
