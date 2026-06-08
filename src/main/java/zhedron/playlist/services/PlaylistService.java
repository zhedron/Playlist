package zhedron.playlist.services;


import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.request.PlaylistRequest;

import java.io.IOException;
import java.util.List;

public interface PlaylistService {
    void savePlaylist(PlaylistRequest playlistRequest, MultipartFile file) throws IOException;

    void addSong(long idSong, long playlistId);

    List<PlaylistDTO> getPlaylistsByArtistNameOrAlbumName(String artistName, String albumName, long userId);

    void changeVisibility(long playlistId, boolean isPublic);

    void deletePlaylist(long playlistId);

    PlaylistDTO findPlaylistById(long playlistId);

    PlaylistDTO getPlaylist(long playlistId, long userId);
}
