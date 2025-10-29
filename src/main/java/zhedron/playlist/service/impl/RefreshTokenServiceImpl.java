package zhedron.playlist.service.impl;

import org.springframework.stereotype.Service;
import zhedron.playlist.entity.RefreshToken;
import zhedron.playlist.entity.User;
import zhedron.playlist.repository.RefreshTokenRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.RefreshTokenService;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final long TOKENEXPIRATION_TIME = 30 * 24 * 60 * 60 * 1000L;

    private final UserRepository userRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }


    @Override
    public RefreshToken generateRefreshToken(String email) {
        RefreshToken refreshToken = new RefreshToken();

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!refreshTokenRepository.existsByUser(optionalUser.get())) {
            refreshToken.setUser(optionalUser.get());
            refreshToken.setRefreshToken(UUID.randomUUID().toString());
            refreshToken.setExpiredAt(new Date(System.currentTimeMillis() + TOKENEXPIRATION_TIME));

            return refreshTokenRepository.save(refreshToken);
        } else {
            RefreshToken foundRefreshToken = refreshTokenRepository.findByUser(optionalUser.get());

            foundRefreshToken.setExpiredAt(new Date(System.currentTimeMillis() + TOKENEXPIRATION_TIME));

            return refreshTokenRepository.save(foundRefreshToken);
        }
    }

    @Override
    public RefreshToken verifyRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.getExpiredAt().before(new Date())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh Token is expired and removed " + refreshToken.getRefreshToken());
        }

        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }
}
