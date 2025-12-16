package com.behpardakht.oauth_server.authorization.security.userDetailService;

import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.repository.UserClientAssignmentRepository;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import com.behpardakht.oauth_server.authorization.security.ClientContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserClientAssignmentRepository assignmentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Long clientId = ClientContextHolder.getClientId();
        if (clientId == null) {
            return new User(user.getUsername(), user.getPassword(),
                    true, true, true, true, Collections.emptyList());
        }
        UserClientAssignment assignment = assignmentRepository.findByUserAndClient_Id(user, clientId)
                .orElseThrow(() -> new UsernameNotFoundException("User not assigned to client"));

        return new CustomUserDetails(user, assignment);
    }
}