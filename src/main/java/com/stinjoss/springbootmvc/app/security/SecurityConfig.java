package com.stinjoss.springbootmvc.app.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((auth) -> auth
                        .requestMatchers(HttpMethod.GET, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/sales").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/products").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/clients").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/sales").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/clients").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/users").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/sales").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/products").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/clients").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/users").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/products").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/clients").hasAnyRole("ADMINISTRADOR", "SELLER")
                        .anyRequest().authenticated())
                //.addFilter(new JwtAuthenticationFilter(authenticationManager()))
                //.addFilter(new JwtValidationFilter(authenticationManager()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }


}
