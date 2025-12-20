package com.behpardakht.oauth_server.authorization.model.dto.otp;

import com.behpardakht.oauth_server.authorization.model.enums.PkceMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = true)
public class SendOtpRequestDto extends BaseOtpRequestDto {

    @NotBlank(message = "{phone_number_is_required}")
    @Pattern(regexp = "^98\\d{10}$", message = "{invalid_phone_number_format}")
    private String phoneNumber;

    @NotBlank(message = "{clientId_is_required}")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "{invalid_clientId_format}")
    private String clientId;

    @Pattern(regexp = "^(https?://.*|[a-zA-Z][a-zA-Z0-9+.-]*://.*)?$", message = "{invalid_redirectUri_format}")
    private String redirectUri;

    @Size(min = 43, max = 128, message = "{invalid_code_challenge_size}")
    @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "{invalid_code_challenge_format}")
    private String codeChallenge;

    private PkceMethod codeChallengeMethod;

    private String scope;
}