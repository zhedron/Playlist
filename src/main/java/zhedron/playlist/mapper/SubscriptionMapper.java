package zhedron.playlist.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import zhedron.playlist.dto.SubscriptionDTO;
import zhedron.playlist.entity.Subscription;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionMapper {
    List<SubscriptionDTO> toSubscriptionsDTO(List<Subscription> subscriptions);

    @Mapping(target = "userId", source = "targetUser.id")
    @Mapping(target = "name", source = "targetUser.name")
    @Mapping(target = "profilePicture", source = "targetUser.profilePicture")
    @Mapping(target = "contentType", source = "targetUser.contentType")
    SubscriptionDTO toSubscriptionDTO(Subscription subscription);
}
