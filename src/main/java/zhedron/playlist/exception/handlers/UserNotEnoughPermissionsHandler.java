package zhedron.playlist.exception.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zhedron.playlist.exception.UserNotEnoughPermissionsException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class UserNotEnoughPermissionsHandler {
    @ExceptionHandler(UserNotEnoughPermissionsException.class)
    public ResponseEntity<Map<String, String>> handleUserNotEnoughPermissionsException(UserNotEnoughPermissionsException ex) {
        Map<String, String> map = new HashMap<>();
        map.put("message", ex.getMessage());

        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
}
