package zhedron.playlist.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zhedron.playlist.exceptions.SubscribedException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class SubscribedExceptionHandler {
    @ExceptionHandler(SubscribedException.class)
    public ResponseEntity<Map<String, Object>> handleSubscribedException(SubscribedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
