package zhedron.playlist.service;

import zhedron.playlist.entity.RefreshToken;
import zhedron.playlist.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken generateRefreshToken(String email);

    RefreshToken verifyRefreshToken(RefreshToken refreshToken);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
