package zhedron.playlist.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import zhedron.playlist.entity.User;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.MyUserDetails;

import java.util.Optional;

@Service
public class UserDetailsImpl implements UserDetailsService {
    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = repository.findByEmail(username);

        return user.map(MyUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("User not found with " + username));
    }
}
