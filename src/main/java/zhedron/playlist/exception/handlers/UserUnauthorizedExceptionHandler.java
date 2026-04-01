package zhedron.playlist.exception.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zhedron.playlist.exception.UserUnauthorizedException;

@RestControllerAdvice
public class UserUnauthorizedExceptionHandler {
    @ExceptionHandler(UserUnauthorizedException.class)
    public ResponseEntity<?> handleUserUnauthorizedException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
