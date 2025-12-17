package com.cinema.infrastructure.config;

import com.cinema.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // public auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/validate").permitAll()

                        // swagger/h2/static
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/static/**",
                                "/public/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()

                        // -------------------------
                        // VISITOR (PUBLIC READ)
                        // -------------------------
                        .requestMatchers(HttpMethod.GET,
                                "/api/programs",
                                "/api/programs/*",
                                "/api/screenings/*",
                                "/api/screenings/by-program"
                        ).permitAll()

                        // -------------------------
                        // ME (any logged in)
                        // -------------------------
                        .requestMatchers("/api/me/**").authenticated()

                        // -------------------------
                        // ADMIN (μόνο user management)
                        // -------------------------
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // =====================================================
                        // PROGRAMS (σύμφωνα με πίνακα)
                        // USER + (SUBMITTER/PROGRAMMER/STAFF) μπορούν CREATE
                        // μόνο PROGRAMMER κάνει management
                        // =====================================================

                        // create program
                        .requestMatchers(HttpMethod.POST, "/api/programs")
                        .hasAnyRole("USER", "SUBMITTER", "PROGRAMMER", "STAFF")

                        // manage program
                        .requestMatchers(HttpMethod.PUT, "/api/programs/*", "/api/programs/*/state").hasRole("PROGRAMMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/programs/*").hasRole("PROGRAMMER")
                        .requestMatchers(HttpMethod.POST, "/api/programs/*/programmers/*", "/api/programs/*/staff/*").hasRole("PROGRAMMER")

                        // =====================================================
                        // SCREENINGS (σύμφωνα με πίνακα)
                        // USER (+ PROGRAMMER/STAFF/SUBMITTER) μπορούν CREATE screening
                        // SUBMITTER: update/submit/withdraw/final-submit own
                        // STAFF: review assigned
                        // PROGRAMMER: assign handler + approve/reject + schedule
                        // =====================================================

                        // create screening (All USER functions για PROGRAMMER/STAFF/SUBMITTER)
                        .requestMatchers(HttpMethod.POST, "/api/screenings")
                        .hasAnyRole("USER", "SUBMITTER", "PROGRAMMER", "STAFF")

                        // SUBMITTER actions
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/*").hasRole("SUBMITTER")
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/*/submit", "/api/screenings/*/withdraw").hasRole("SUBMITTER")
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/*/final-submit").hasRole("SUBMITTER")
                        .requestMatchers(HttpMethod.GET, "/api/screenings/by-submitter").hasRole("SUBMITTER")

                        // STAFF
                        .requestMatchers(HttpMethod.GET, "/api/screenings/by-staff").hasRole("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/*/review").hasRole("STAFF")

                        // PROGRAMMER
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/*/handler/*").hasRole("PROGRAMMER")
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/*/approve").hasRole("PROGRAMMER")
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/*/reject").hasRole("PROGRAMMER")
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/*/schedule").hasRole("PROGRAMMER")

                        // everything else
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
