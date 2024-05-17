package wander.wise.application.exception.custom;

public class MapsServiceException extends RuntimeException {
    public MapsServiceException(String message, Exception exception) {
        super(message, exception);
    }
}
