package zhedron.playlist.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zhedron.playlist.exceptions.RefreshTokenNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RefreshTokenNotFoundExceptionHandler {
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handeException(RefreshTokenNotFoundException e) {
        Map<String, Object> response = new HashMap<>();

        response.put("message", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
