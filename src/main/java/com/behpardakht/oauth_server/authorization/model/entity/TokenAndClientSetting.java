package com.behpardakht.oauth_server.authorization.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TokenAndClientSetting {

    //Client Setting Fields -----------------------
    @Column(name = "require_proof_key")
    private Boolean requireProofKey;

    @Column(name = "require_authorization_consent")
    private Boolean requireAuthorizationConsent;


    //Token Setting Fields --------------------------
    @Column(name = "access_token_time_to_live")
    private Long accessTokenTimeToLive;

    @Column(name = "x509_certificate_bound_access_tokens")
    private Boolean x509CertificateBoundAccessTokens;

    @Column(name = "refresh_token_time_to_live")
    private Long refreshTokenTimeToLive;

    @Column(name = "reuse_refresh_tokens")
    private Boolean reuseRefreshTokens;

    @Column(name = "id_token_signature_algorithm")
    private String idTokenSignatureAlgorithm;

    @Column(name = "authorization_code_time_to_live")
    private Long authorizationCodeTimeToLive;

    @Column(name = "device_code_time_to_live")
    private Long deviceCodeTimeToLive;
}