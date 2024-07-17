package com.gettimhired.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain filterChainLocal(HttpSecurity http) throws Exception {
        return http
                .securityMatchers(matchers -> {
                    matchers.requestMatchers("/api/**");
                    matchers.requestMatchers("/graphql");
                    matchers.requestMatchers("/swagger-ui/**");
                    matchers.requestMatchers("/");
                })
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(basic -> basic.init(http))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/api/**").authenticated();
                    authorize.requestMatchers("/graphql").authenticated();
                    authorize.requestMatchers("/swagger-ui/**").permitAll();
                    authorize.requestMatchers("/").permitAll();
                    authorize.anyRequest().permitAll();
                })
                .userDetailsService(userDetailsService())
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user1")
                .password(passwordEncoderTest().encode("password"))
                .roles("USER")
                .build());
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoderTest() {
        return new BCryptPasswordEncoder();
    }
}
