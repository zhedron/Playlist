package zhedron.playlist.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zhedron.playlist.exceptions.PhoneExistException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class PhoneExistExceptionHandler {
    @ExceptionHandler(PhoneExistException.class)
    public ResponseEntity<Map<String, Object>> handlePhoneExistException(PhoneExistException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
