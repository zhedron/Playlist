package zhedron.playlist.service;


import zhedron.playlist.dto.PlaylistDTO;

import java.util.List;

public interface PlaylistService {
    void addSong(long idSong, boolean isPublic);

    List<PlaylistDTO> getPlaylistsByArtistNameOrAlbumName(String artistName, String albumName, long userId);
}
