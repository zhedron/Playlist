package zhedron.playlist.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zhedron.playlist.exceptions.UserBlockedException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class UserBlockedHandler {
    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<Map<String, String>> handleException(UserBlockedException blocked) {
        Map<String, String> errors = new HashMap<>();

        errors.put("message", blocked.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);
    }
}
