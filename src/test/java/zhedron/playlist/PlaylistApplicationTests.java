package zhedron.playlist;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import zhedron.playlist.service.AESEncryptionService;
import zhedron.playlist.service.impl.UserServiceImpl;

@SpringBootTest
class PlaylistApplicationTests {
    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private AESEncryptionService aesEncryptionService;

    @Test
    void contextLoads() {
    }

}
