package com.behpardakht.oauth_server.authorization.model.dto.otp.request;

import com.behpardakht.oauth_server.authorization.model.enums.PkceMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InitOtpRequestDto extends BaseOtpRequestDto {
    @NotBlank(message = "Client ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Client ID can only contain letters, numbers, underscores and hyphens")
    private String clientId;

    @Pattern(regexp = "^(https?://.*|[a-zA-Z][a-zA-Z0-9+.-]*://.*)?$",
            message = "Redirect URI must be a valid URL (http, https) or custom scheme")
    private String redirectUri;

    @Size(min = 43, max = 128, message = "Code challenge must be between 43 and 128 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]*$",
            message = "Code challenge must be base64url encoded (letters, numbers, underscore, hyphen)")
    private String codeChallenge;

    private PkceMethod codeChallengeMethod;

    private String scope;
}