package zhedron.playlist.service;

import zhedron.playlist.entity.Song;

public interface SongService {
    Song save(Song song);

    Song getSongById(long id);
}
