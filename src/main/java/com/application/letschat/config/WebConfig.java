package com.application.letschat.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class WebConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerCustomizer() {
        return factory -> {
            factory.addErrorPages(
                    new ErrorPage(HttpStatus.BAD_REQUEST, "/400.html"),          // 400 Bad Request
                    new ErrorPage(HttpStatus.UNAUTHORIZED, "/401.html"),         // 401 Unauthorized
                    new ErrorPage(HttpStatus.FORBIDDEN, "/403.html"),            // 403 Forbidden
                    new ErrorPage(HttpStatus.NOT_FOUND, "/404.html"),            // 404 Not Found
                    new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/405.html"),   // 405 Method Not Allowed
                    new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html"),// 500 Internal Server Error
                    new ErrorPage(HttpStatus.SERVICE_UNAVAILABLE, "/503.html")   // 503 Service Unavailable
            );
        };
    }
}