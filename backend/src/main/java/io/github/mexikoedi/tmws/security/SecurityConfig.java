/**
 * Diese Klasse konfiguriert die Sicherheitseinstellungen der Anwendung, einschließlich der
 * Authentifizierung und Autorisierung.
 */
package io.github.mexikoedi.tmws.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  /**
   * Definiert den PasswordEncoder, der für die sichere Speicherung von Passwörtern verwendet wird.
   *
   * @return Ein BCryptPasswordEncoder-Bean, der für die Passwortverschlüsselung verwendet wird.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Konfiguriert die Sicherheitsfilterkette, um die Authentifizierungs- und Autorisierungsregeln
   * festzulegen. Es werden bestimmte Endpunkte für die öffentliche Nutzung freigegeben, während
   * alle anderen Endpunkte eine Authentifizierung erfordern. Außerdem wird die CSRF-Schutz
   * deaktiviert, CORS aktiviert und die Session-Management-Strategie auf stateless gesetzt, um die
   * Verwendung von JWTs zu unterstützen. Der JwtAuthFilter wird vor dem
   * UsernamePasswordAuthenticationFilter hinzugefügt, um die JWT-Authentifizierung zu ermöglichen
   * und die Sicherheit der Anwendung zu gewährleisten.
   *
   * @param http Das HttpSecurity-Objekt, das für die Konfiguration der Sicherheitsregeln verwendet
   *     wird.
   * @param jwtFilter Der JwtAuthFilter, der für die Verarbeitung von JWTs in den Anfragen
   *     verantwortlich ist.
   * @return Ein SecurityFilterChain-Bean, der die konfigurierten Sicherheitsregeln enthält und von
   *     Spring Security verwendet wird.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtFilter) {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/verify-email",
                        "/api/auth/password-reset",
                        "/api/auth/reset-password",
                        "/api/health",
                        "/api/h2-console/**",
                        "/api/ws/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
