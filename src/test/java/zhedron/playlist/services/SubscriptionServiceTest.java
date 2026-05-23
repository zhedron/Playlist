package zhedron.playlist.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zhedron.playlist.entity.Subscription;
import zhedron.playlist.entity.User;
import zhedron.playlist.exceptions.SubscribedException;
import zhedron.playlist.exceptions.UserNotFoundException;
import zhedron.playlist.repository.SubscriptionRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.services.impl.SubscriptionServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    void subscribeToUserShouldSaveSubscriptionForFoundUser() {
        User currentUser = new User();
        currentUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(subscriptionRepository.existsBySubscriberIdAndTargetUserId(1L, 2L)).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        subscriptionService.subscribeToUser(2L);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void subscribeToUserShouldThrowWhenAlreadySubscribed() {
        User currentUser = new User();
        currentUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(subscriptionRepository.existsBySubscriberIdAndTargetUserId(1L, 2L)).thenReturn(true);

        SubscribedException exception = assertThrows(
                SubscribedException.class,
                () -> subscriptionService.subscribeToUser(2L)
        );

        assertEquals("You're already subscribed!", exception.getMessage());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void subscribeToUserShouldThrowWhenTargetUserMissing() {
        User currentUser = new User();
        currentUser.setId(1L);

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> subscriptionService.subscribeToUser(2L)
        );

        assertEquals("User not found!", exception.getMessage());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void unsubscribeFromUserShouldDeleteSubscriptionByTargetUserId() {
        User currentUser = new User();
        currentUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(subscriptionRepository.existsBySubscriberIdAndTargetUserId(1L, 2L)).thenReturn(true);

        subscriptionService.unsubscribeFromUser(2L);

        verify(subscriptionRepository).deleteBySubscriberIdAndTargetUserId(1L, 2L);
    }

    @Test
    void unsubscribeFromUserShouldThrowWhenNotSubscribed() {
        User currentUser = new User();
        currentUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(subscriptionRepository.existsBySubscriberIdAndTargetUserId(1L, 2L)).thenReturn(false);

        SubscribedException exception = assertThrows(
                SubscribedException.class,
                () -> subscriptionService.unsubscribeFromUser(2L)
        );

        assertEquals("You're not subscribed!", exception.getMessage());
        verify(subscriptionRepository, never()).deleteBySubscriberIdAndTargetUserId(1L, 2L);
    }

    @Test
    void unsubscribeFromUserShouldThrowWhenTargetUserMissing() {
        User currentUser = new User();
        currentUser.setId(1L);

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> subscriptionService.unsubscribeFromUser(2L)
        );

        assertEquals("User not found!", exception.getMessage());
        verify(subscriptionRepository, never()).deleteBySubscriberIdAndTargetUserId(1L, 2L);
    }
}
