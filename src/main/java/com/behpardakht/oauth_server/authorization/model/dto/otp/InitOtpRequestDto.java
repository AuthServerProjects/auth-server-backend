package com.behpardakht.oauth_server.authorization.model.dto.otp;

import com.behpardakht.oauth_server.authorization.model.enums.PkceMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InitOtpRequestDto {
    @NotBlank(message = "Client ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Client ID can only contain letters, numbers, underscores and hyphens")
    private String clientId;

    @Size(max = 500, message = "State must not exceed 500 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]*$", message = "State can only contain letters, numbers, underscores, dots and hyphens")
    private String state;

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