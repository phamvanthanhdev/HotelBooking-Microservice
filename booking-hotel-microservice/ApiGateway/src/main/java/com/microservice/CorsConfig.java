package com.microservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig{
    @Bean
    public CorsWebFilter corsWebFilter() {

        final CorsConfiguration corsConfig = new CorsConfiguration();
        //corsConfig.setAllow("http://127.0.0.1:5173");
//        corsConfig.addAllowedOrigin("http://127.0.0.1:5173");
//        corsConfig.setMaxAge(3600L);
//        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST"));
        //corsConfig.addAllowedHeader("*");

        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOrigin("http://127.0.0.1:5173");
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        corsConfig.addAllowedHeader("origin");
        corsConfig.addAllowedHeader("content-type");
        corsConfig.addAllowedHeader("accept");
        corsConfig.addAllowedHeader("authorization");
        corsConfig.addAllowedHeader("cookie");

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
