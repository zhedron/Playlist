package zhedron.playlist.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zhedron.playlist.exceptions.ArtistAndAlbumNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ArtistNameAndAlbumNameNotFoundHandler {
    @ExceptionHandler(ArtistAndAlbumNotFoundException.class)
    public ResponseEntity<Map<String, String>> ArtistNameAndAlbumNameNotFound(ArtistAndAlbumNotFoundException ex) {
        Map<String, String> map = new HashMap<>();
        map.put("message", ex.getMessage());

        return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
    }
}
