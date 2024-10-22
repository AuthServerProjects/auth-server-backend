package com.behpardakht.side_pay.auth.security.common.config;

import com.behpardakht.side_pay.auth.security.authorizationServer.otp.OtpAuthenticationFilter;
import com.behpardakht.side_pay.auth.security.authorizationServer.otp.OtpJwtAuthenticationConverter;
import com.behpardakht.side_pay.auth.security.authorizationServer.otp.OTPAuthenticationProvider;
import com.behpardakht.side_pay.auth.security.resourceServer.JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    String jwkSetUri;

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final OtpJwtAuthenticationConverter otpJwtAuthenticationConverter;
    private final OTPAuthenticationProvider otpAuthenticationProvider;
//    private final OAuth2ClientCredentialsAuthenticationProvider oAuth2ClientCredentialsAuthenticationProvider;


    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OtpAuthenticationFilter otpAuthenticationFilter) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http
                .getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());

        http
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));


        http.addFilterBefore(otpAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorization ->
                                authorization
                                        .requestMatchers(
                                                "/",
                                                "/user/register",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**").permitAll()
                                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .oauth2ResourceServer(
                        oAuth2ResourceServerConfigurer ->
                                oAuth2ResourceServerConfigurer
                                        .jwt(
                                                jwtConfigurer ->
                                                        jwtConfigurer
                                                                .jwkSetUri(jwkSetUri)
                                                                .jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    @Bean
    public OAuth2AuthorizationServerConfigurer oAuth2AuthorizationServerConfigurer(AuthenticationProvider authenticationProvider) {
        return new OAuth2AuthorizationServerConfigurer()
                .tokenEndpoint(
                        tokenEndPoint ->
                                tokenEndPoint
                                        .accessTokenRequestConverter(new OtpJwtAuthenticationConverter())
                                        .authenticationProvider(authenticationProvider));
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
//                .authenticationProvider(new OAuth2ClientCredentialsAuthenticationProvider())
                .authenticationProvider(otpAuthenticationProvider)
                .build();
    }

    @Bean
    public OtpAuthenticationFilter otpAuthenticationFilter(AuthenticationManager authenticationManager) {
        OtpAuthenticationFilter filter =
                new OtpAuthenticationFilter(otpJwtAuthenticationConverter, otpAuthenticationProvider);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }
}