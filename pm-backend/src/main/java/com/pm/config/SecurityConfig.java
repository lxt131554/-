package com.pm.config;

import com.pm.security.LoginFailureHandler;
import com.pm.security.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health", "/actuator/health", "/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((req, resp, ex) -> {
                    resp.setStatus(401);
                    resp.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                    resp.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
                })
                .accessDeniedHandler((req, resp, ex) -> {
                    resp.setStatus(403);
                    resp.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                    resp.getWriter().write("{\"code\":403,\"message\":\"没有权限\"}");
                })
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, resp, auth) -> {
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"code\":200,\"message\":\"已退出\"}");
                })
                .invalidateHttpSession(true)
                .permitAll()
            );

        return http.build();
    }
}
