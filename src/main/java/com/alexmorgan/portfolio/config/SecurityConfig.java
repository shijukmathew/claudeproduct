package com.alexmorgan.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        // Saving and deleting favourites requires login
                        .requestMatchers("/cities/**").authenticated()
                        // Everything else (search, home) is public
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth.defaultSuccessUrl("/", true))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                // Allow H2 console in local dev
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .build();
    }
}
