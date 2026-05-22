package zhedron.playlist.services;

public interface SubscriptionService {
    void subscribeToUser(long userId);

    void unsubscribeFromUser(long userId);
}
