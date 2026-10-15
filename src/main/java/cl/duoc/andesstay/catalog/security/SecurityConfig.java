package cl.duoc.andesstay.catalog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Valida el JWT de Azure AD y autoriza por rol.
 *
 *  - Consultar alojamientos: cualquier usuario autenticado con rol.
 *  - Crear / modificar alojamientos: ADMIN u OPERADOR.
 *  - Desactivar alojamientos: ADMIN.
 *  - Tickets de limpieza: ADMIN u OPERADOR.
 */
@Configuration
public class SecurityConfig {

    private static final String ACCOMMODATIONS = "/api/catalog/accommodations";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        .requestMatchers(HttpMethod.GET, ACCOMMODATIONS, ACCOMMODATIONS + "/*")
                        .hasAnyRole("ADMIN", "OPERADOR", "CLIENTE", "AUDITOR")

                        .requestMatchers(HttpMethod.POST, ACCOMMODATIONS)
                        .hasAnyRole("ADMIN", "OPERADOR")

                        .requestMatchers(HttpMethod.PUT, ACCOMMODATIONS + "/*")
                        .hasAnyRole("ADMIN", "OPERADOR")

                        .requestMatchers(HttpMethod.DELETE, ACCOMMODATIONS + "/*")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/catalog/housekeeping-tickets/**")
                        .hasAnyRole("ADMIN", "OPERADOR")

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(AzureRolesConverter.create())));

        return http.build();
    }
}
