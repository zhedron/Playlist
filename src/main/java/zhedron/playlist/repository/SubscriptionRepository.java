package zhedron.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zhedron.playlist.entity.Subscription;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsBySubscriberIdAndTargetUserId(long subscriberId, long targetUserId);

    void deleteBySubscriberIdAndTargetUserId(long subscriberId, long targetUserId);

    List<Subscription> getSubscriptionsBySubscriberId(long subscriberId);
}
