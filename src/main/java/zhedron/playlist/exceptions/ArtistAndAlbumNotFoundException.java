package zhedron.playlist.exceptions;

public class ArtistAndAlbumNotFoundException extends RuntimeException {
  public ArtistAndAlbumNotFoundException(String message) {
    super(message);
  }
}
