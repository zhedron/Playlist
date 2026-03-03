package zhedron.playlist.exception;

public class UserNotEnoughPermissionsException extends RuntimeException {
    public UserNotEnoughPermissionsException(String message) {
        super(message);
    }
}
