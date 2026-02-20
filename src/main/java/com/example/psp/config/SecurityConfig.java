package com.example.psp.config;

import com.example.psp.filter.JwtAuthFilter;
import com.example.psp.service.PSPService;
import com.example.psp.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
public class SecurityConfig {
    private final TokenService tokenService;
    private final PSPService pspService;

    @Autowired
    private CorsConfig corsConfig;

    public SecurityConfig(TokenService tokenService,PSPService pspService) {
        this.tokenService = tokenService;
        this.pspService = pspService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf ->csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurer()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/register",
                                "/api/user/login",
                                "/health")
                        .permitAll() // public endpoints
                        .anyRequest().authenticated() // protect all other endpoints
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT, no session
                );

        http.addFilterBefore(new JwtAuthFilter(tokenService,pspService),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}
