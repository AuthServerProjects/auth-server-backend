package com.behpardakht.oauth_server.authorization.model.entity;

import com.behpardakht.oauth_server.authorization.model.enums.AuthenticationMethodTypes;
import com.behpardakht.oauth_server.authorization.model.enums.AuthorizationGrantTypes;
import com.behpardakht.oauth_server.authorization.model.enums.ScopeTypes;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client")
@EqualsAndHashCode(callSuper = true)
public class Client extends BaseEntity {

    @Column(name = "registered_client_id")
    private String registeredClientId;

    @Column(name = "client_id", unique = true)
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_auth_methods", joinColumns = @JoinColumn(name = "client_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method")
    private Set<AuthenticationMethodTypes> clientAuthenticationMethods;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_grant_types", joinColumns = @JoinColumn(name = "client_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type")
    private Set<AuthorizationGrantTypes> authorizationGrantTypes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "redirect_uri")
    private Set<String> redirectUris;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_scopes", joinColumns = @JoinColumn(name = "client_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "scope")
    private Set<ScopeTypes> scopes;

    @Embedded
    private TokenAndClientSetting setting;
}