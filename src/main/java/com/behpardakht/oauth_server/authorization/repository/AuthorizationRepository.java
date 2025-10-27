package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Authorizations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorizationRepository extends JpaRepository<Authorizations, String> {

    Optional<Authorizations> findByAuthorizationId(String authorizationId);

    Optional<Authorizations> findByAccessToken(String accessToken);

    Optional<Authorizations> findByRefreshToken(String refreshToken);

    Optional<Authorizations> findByAuthorizationCode(String authorizationCode);

}