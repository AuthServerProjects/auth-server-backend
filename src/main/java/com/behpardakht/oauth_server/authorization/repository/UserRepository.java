package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {

    //    @EntityGraph(attributePaths = {"roles"})
    Optional<Users> findByUsername(String username);

    Optional<Users> findByPhoneNumber(String phoneNumber);
}