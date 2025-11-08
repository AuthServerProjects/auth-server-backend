package com.behpardakht.oauth_server.authorization.security.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;
import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.URL_PREFIX;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    String allowedOrigins;

    @Value("${cors.allowed-methods}")
    String allowedMethods;

    @Value("${cors.allowed-headers}")
    String allowedHeaders;

    @Value("${cors.exposed-headers}")
    String exposedHeaders;

    @Value("${cors.allowed-credentials}")
    String allowCredentials;

    @Value("${cors.max-age}")
    Long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of(allowedMethods.split(",")));
        configuration.setAllowedHeaders(List.of(allowedHeaders.split(",")));
        configuration.setExposedHeaders(List.of(exposedHeaders.split(",")));
        configuration.setAllowCredentials((Boolean.parseBoolean(allowCredentials)));
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(API_PREFIX + "/**", configuration);
        source.registerCorsConfiguration(URL_PREFIX + "/**", configuration);
        return source;
    }
}