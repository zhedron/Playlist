package zhedron.playlist.exception;

public class ArtistAndAlbumNotFoundException extends RuntimeException {
  public ArtistAndAlbumNotFoundException(String message) {
    super(message);
  }
}
