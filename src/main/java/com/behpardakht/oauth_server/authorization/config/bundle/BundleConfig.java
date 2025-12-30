package com.behpardakht.oauth_server.authorization.config.bundle;

import com.behpardakht.oauth_server.authorization.config.Properties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class BundleConfig {
    private final Properties properties;

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag(properties.getLocalization().getLanguage()));
        resolver.setSupportedLocales(List.of(
                Locale.forLanguageTag("fa"),
                Locale.forLanguageTag("en")
        ));
        return resolver;
    }

    @PostConstruct
    public void init() {
        // Set the default locale for the entire application
        // This is used for async methods, scheduled tasks, etc.
        Locale locale = Locale.forLanguageTag(properties.getLocalization().getLanguage());
        Locale.setDefault(locale);
        LocaleContextHolder.setDefaultLocale(locale);
    }
}