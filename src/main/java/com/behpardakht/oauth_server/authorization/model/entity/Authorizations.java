package com.behpardakht.oauth_server.authorization.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "authorizations")
public class Authorizations extends BaseEntity {

    @Column(name = "authorization_id")
    private String authorizationId;

    @Column(name = "authorization_code")
    private String authorizationCode;

    @Column(name = "authorization_code_issued_at")
    private Instant authorizationCodeIssuedAt;

    @Column(name = "authorization_code_expires_at")
    private Instant authorizationCodeExpiresAt;

    @Column(name = "authorization_code_consumed")
    private Boolean authorizationCodeConsumed;

    @Column(name = "authorization_code_consumed_at")
    private Instant authorizationCodeConsumedAt;

    @Column(name = "access_token", length = 1000)
    private String accessToken;

    @Column(name = "access_token_issued_at")
    private Instant accessTokenIssuedAt;

    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;

    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    @Column(name = "refresh_token_issued_at")
    private Instant refreshTokenIssuedAt;

    @Column(name = "refresh_token_expires_at")
    private Instant refreshTokenExpiresAt;

    @Column(name = "principal_name")
    private String principalName;

    @Column(name = "registered_client_id")
    private String registeredClientId;

    @Column(name = "authorization_attributes", length = 2000)
    private String authorizationAttributes;
}