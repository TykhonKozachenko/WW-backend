package wander.wise.application.exception.custom;

public class StorageException extends RuntimeException {
    public StorageException(String message, Exception exception) {
        super(message, exception);
    }
}
