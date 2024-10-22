package com.behpardakht.side_pay.auth.repository;

import com.behpardakht.side_pay.auth.model.entity.Authorizations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorizationRepository extends JpaRepository<Authorizations, String> {

    Optional<Authorizations> findByAccessToken(String accessToken);

    Optional<Authorizations> findByRefreshToken(String refreshToken);
}