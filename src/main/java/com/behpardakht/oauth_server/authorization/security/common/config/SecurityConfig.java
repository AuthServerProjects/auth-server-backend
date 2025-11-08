package com.behpardakht.oauth_server.authorization.security.common.config;

import com.behpardakht.oauth_server.authorization.security.resourceServer.JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.ClientSecretAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;
import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.URL_PREFIX;

@Configuration
@RequiredArgsConstructor
@DependsOn("corsConfig")
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    String jwkSetUri;

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                                      OAuth2TokenGenerator<?> tokenGenerator) throws Exception {

        OAuth2AuthorizationServerConfigurer authServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authServerConfigurer.getEndpointsMatcher())
                .with(authServerConfigurer, authServer ->
                        authServer
                                .tokenGenerator(tokenGenerator)
                                .oidc(Customizer.withDefaults())
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/oauth2/authorize").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          AuthenticationManager authenticationManager,
                                                          CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                API_PREFIX + "/**",
                                URL_PREFIX + "/**",
                                "/oauth2/token",
                                "/oauth2/introspect",
                                "/oauth2/revoke",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                )
                .authorizeHttpRequests(
                        authorization ->
                                authorization
                                        .requestMatchers(
                                                "/otp/**",
                                                "api/otp/**",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**").permitAll()
                                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .authenticationManager(authenticationManager)
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt
                                        .jwkSetUri(jwkSetUri)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       RegisteredClientRepository registeredClientRepository,
                                                       OAuth2AuthorizationService authorizationService,
                                                       OAuth2TokenGenerator<?> tokenGenerator) throws Exception {
        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(new OAuth2ClientCredentialsAuthenticationProvider(authorizationService, tokenGenerator))
                .authenticationProvider(new OAuth2AuthorizationCodeAuthenticationProvider(authorizationService, tokenGenerator))
                .authenticationProvider(new OAuth2RefreshTokenAuthenticationProvider(authorizationService, tokenGenerator))
                .authenticationProvider(new ClientSecretAuthenticationProvider(registeredClientRepository, authorizationService))
                .build();
    }
}