package zhedron.playlist.exceptions;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
