package zhedron.playlist.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zhedron.playlist.exceptions.UserExistException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class UserExistHandler {
    @ExceptionHandler(UserExistException.class)
    public ResponseEntity<Map<String, String>> handeUserExistException(UserExistException e) {
        Map<String, String> map = new HashMap<>();
        map.put("message", e.getMessage());

        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
}
