package zhedron.playlist.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zhedron.playlist.entity.Subscription;
import zhedron.playlist.entity.User;
import zhedron.playlist.exceptions.SubscribedException;
import zhedron.playlist.exceptions.UserNotFoundException;
import zhedron.playlist.repository.SubscriptionRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.services.SubscriptionService;
import zhedron.playlist.services.UserService;

import java.util.Optional;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, UserRepository userRepository, UserService userService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void subscribeToUser(long userId) {
        User currentUser = userService.getCurrentUser();

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found!");
        }

        User userFound = user.get();

        if (subscriptionRepository.existsBySubscriberIdAndTargetUserId(currentUser.getId(), userFound.getId())) {
            throw new SubscribedException("You're already subscribed!");
        }

        Subscription subscription = new Subscription();

        subscription.setSubscriber(currentUser);
        subscription.setTargetUser(userFound);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        log.info("User subscribed to User {}", savedSubscription.getTargetUser().getId());
    }

    @Override
    @Transactional
    public void unsubscribeFromUser(long userId) {
        User currentUser = userService.getCurrentUser();

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found!");
        }

        User userFound = user.get();

        if (!subscriptionRepository.existsBySubscriberIdAndTargetUserId(currentUser.getId(), userFound.getId())) {
            throw new SubscribedException("You're not subscribed!");
        }

        subscriptionRepository.deleteBySubscriberIdAndTargetUserId(currentUser.getId(), userFound.getId());
    }
}
