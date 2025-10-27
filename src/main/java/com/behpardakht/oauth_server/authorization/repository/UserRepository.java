package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    //    @EntityGraph(attributePaths = {"roles"})
    Optional<Users> findByUsername(String username);

    Optional<Users> findByPhoneNumber(String phoneNumber);
}