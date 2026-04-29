package zhedron.playlist.exceptions;

public class UserNotEnoughPermissionsException extends RuntimeException {
    public UserNotEnoughPermissionsException(String message) {
        super(message);
    }
}
