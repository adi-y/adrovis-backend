    package com.adrovis.adrovis_backend.config;

    import org.springframework.web.filter.CorsFilter;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

    import java.util.List;

    @Configuration
    public class CorsConfig {

        private final CorsProperties corsProperties;

        public CorsConfig(CorsProperties corsProperties) {
            this.corsProperties = corsProperties;
        }

        @Bean
        public CorsFilter corsFilter() {

            CorsConfiguration configuration = new CorsConfiguration();

            configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());

            configuration.setAllowedMethods(List.of(
                    "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
            ));

            configuration.setAllowedHeaders(List.of("*"));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);

            UrlBasedCorsConfigurationSource source =
                    new UrlBasedCorsConfigurationSource();

            source.registerCorsConfiguration("/**", configuration);

            return new CorsFilter(source);
        }
    }
