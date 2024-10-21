package com.behpardakht.side_pay.auth.repository;

import com.behpardakht.side_pay.auth.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    //    @EntityGraph(attributePaths = {"roles"})
    Optional<Users> findByUsername(String username);

    Optional<Users> findByPhoneNumber(String phoneNumber);
}