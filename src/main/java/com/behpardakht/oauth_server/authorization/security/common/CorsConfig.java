package com.behpardakht.oauth_server.authorization.security.common;

import com.behpardakht.oauth_server.authorization.config.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.*;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final Properties properties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(properties.getCors().getAllowedOrigins().split(",")));
        configuration.setAllowedMethods(List.of(properties.getCors().getAllowedMethods().split(",")));
        configuration.setAllowedHeaders(List.of(properties.getCors().getAllowedHeaders().split(",")));
        configuration.setExposedHeaders(List.of(properties.getCors().getExposedHeaders().split(",")));
        configuration.setAllowCredentials((properties.getCors().isAllowedCredentials()));
        configuration.setMaxAge(properties.getCors().getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(API_PREFIX + "/**", configuration);
        source.registerCorsConfiguration(URL_PREFIX + "/**", configuration);
        source.registerCorsConfiguration(ADMIN_PREFIX + "/**", configuration);
        return source;
    }
}