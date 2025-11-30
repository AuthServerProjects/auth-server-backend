package com.behpardakht.oauth_server.authorization.model.dto.client;

import lombok.Data;

@Data
public class TokenAndClientSettingDto {

    private Long id;

    //Client Setting Fields -----------------------
    private Boolean requireProofKey;
    private Boolean requireAuthorizationConsent;

    //Token Setting Fields --------------------------
    private Long accessTokenTimeToLive;
    private Boolean x509CertificateBoundAccessTokens;
    private Long refreshTokenTimeToLive;
    private Boolean reuseRefreshTokens;
    private String idTokenSignatureAlgorithm;
    private Long authorizationCodeTimeToLive;
    private Long deviceCodeTimeToLive;
}