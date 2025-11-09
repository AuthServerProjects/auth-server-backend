package com.behpardakht.oauth_server.authorization.security.common;

import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = findUserByUsername(username);
        return new User(
                users.getUsername(),
                users.getPassword(),
                users.isEnabled(),
                users.isAccountNonExpired(),
                users.isCredentialsNonExpired(),
                users.isAccountNonLocked(),
                users.getAuthorities());
    }

    private Users findUserByUsername(String username) {
        if (username != null) {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));
        } else {
            throw new IllegalArgumentException("username is null");
        }
    }

    private Set<Role> map(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().filter(Objects::nonNull)
                .map(grantedAuthority ->
                        new Role(grantedAuthority.getAuthority().replace("ROLE_", "")))
                .collect(Collectors.toSet());
    }
}