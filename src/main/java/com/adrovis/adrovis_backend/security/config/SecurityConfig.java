package com.adrovis.adrovis_backend.security.config;

import com.adrovis.adrovis_backend.security.entity.AdminUser;
import com.adrovis.adrovis_backend.security.handler.JwtAccessDeniedHandler;
import com.adrovis.adrovis_backend.security.handler.JwtAuthenticationEntryPoint;
import com.adrovis.adrovis_backend.security.jwt.JwtAuthenticationFilter;
import com.adrovis.adrovis_backend.security.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AdminUserRepository adminUserRepository;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. MUST BE ENABLED for Spring Security to process CORS headers
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 2. Allow all browser preflight OPTIONS requests unconditionally
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public authentication APIs
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Public documentation & health checks
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/health"
                        ).permitAll()

                        // Public Webhooks & Candidate Links
                        .requestMatchers(
                                "/api/v1/webhooks/**",
                                "/api/v1/applications/*/availability",
                                "/api/v1/applications/*/interview"
                        ).permitAll()

                        // Protected admin APIs
                        .requestMatchers(

                                "/api/v1/contact/leads",
                                "/api/v1/admin/payments/applications/{applicationId}/link",
                                "/api/v1/admin/payments/applications/{applicationId}",
                                "/api/v1/applications/{applicationId}",
                                "/api/v1/applications/status/{status}",
                                "/api/v1/applications/job/{jobId}",
                                "/api/v1/applications/{applicationId}/interview",
                                "/api/v1/admin/interviews/{applicationId}/schedule",
                                "/api/v1/admin/interviews/{applicationId}/request-availability",
                                "/api/v1/admin/interviews/{applicationId}/reschedule",
                                "/api/v1/admin/interviews/{applicationId}/outcome",
                                "/api/v1/admin/interviews/{applicationId}/cancel",
                                "/api/v1/admin/interviews",
                                "/api/v1/admin/interviews/{applicationId}",
                                "/api/v1/admin/newsletter/subscribers",
                                "/api/v1/admin/newsletter/subscribers/{subscriberId}"

                        ).hasRole("ADMIN")

                        // Everything else remains public
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins for local dev and production
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.onrender.com",
                "https://adrovis.com",
                "https://www.adrovis.com"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Razorpay-Signature",
                "x-razorpay-event-id"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username ->
                adminUserRepository
                        .findByEmailIgnoreCase(username)
                        .map(this::toUserDetails)
                        .orElseThrow(() ->
                                new UsernameNotFoundException("Admin user not found.")
                        );
    }

    private UserDetails toUserDetails(AdminUser adminUser) {
        return User.builder()
                .username(adminUser.getEmail())
                .password(adminUser.getPasswordHash())
                .roles(adminUser.getRole().name())
                .disabled(!adminUser.isEnabled())
                .build();
    }
}
