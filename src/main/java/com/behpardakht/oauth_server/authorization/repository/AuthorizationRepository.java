package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Authorizations;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorizationRepository extends JpaRepository<Authorizations, Long>, JpaSpecificationExecutor<Authorizations> {

    Optional<Authorizations> findByAuthorizationId(String authorizationId);

    void deleteByAuthorizationId(String authorizationId);

    Optional<Authorizations> findByAccessToken(String accessToken);

    Optional<Authorizations> findByRefreshToken(String refreshToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Authorizations> findByAuthorizationCode(String authorizationCode);

    int deleteByAuthorizationCodeExpiresAtBeforeAndAccessTokenIsNull(Instant expirationTime);

    List<Authorizations> findByPrincipalName(String principalName);

    Long countByAccessTokenExpiresAtAfter(Instant time);

    List<Authorizations> findByPrincipalNameAndRegisteredClientId(String principalName, String registeredClientId);
}