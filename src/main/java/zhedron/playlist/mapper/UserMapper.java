package zhedron.playlist.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.SubscriptionDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Subscription;
import zhedron.playlist.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = SongMapper.class)
public interface UserMapper {
    @Mapping(target = "isHiddenPhone", source = "hiddenPhone")
    @Mapping(target = "subscriptionsDTO", source = "subscriptions")
    UserDTO userToUserDTO(User user);

    @Mapping(target = "isPublic", source = "public")
    PlaylistDTO playlistToPlaylistDTO(Playlist playlist);

    @Mapping(target = "userId", source = "targetUser.id")
    @Mapping(target = "name", source = "targetUser.name")
    @Mapping(target = "about", source = "targetUser.about")
    @Mapping(target = "profilePicture", source = "targetUser.profilePicture")
    @Mapping(target = "contentType", source = "targetUser.contentType")
    SubscriptionDTO subscriptionToSubscriptionDTO(Subscription subscription);
}
