package uk.gov.digital.ho.hocs.extracts.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class JWTSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests(auth -> auth
                        .antMatchers("/export/custom/*/refresh", "/extracts/case/*/delete", "/actuator/health/**").permitAll()
                        .antMatchers("/export/MIN*").hasRole("DCU_EXPORT_USER")
                        .antMatchers("/export/DTEN*").hasRole("DCU_EXPORT_USER")
                        .antMatchers("/export/WCS*").hasRole("WCS_EXPORT_USER")
                        .antMatchers("/export/MPAM*").hasRole("MPAM_EXPORT_USER")
                        .antMatchers("/export/MTS*").hasRole("MPAM_EXPORT_USER")
                        .antMatchers("/export/FOI*").hasRole("FOI_EXPORT_USER")
                        .antMatchers("/export/TRO*").hasRole("DCU_EXPORT_USER")
                        .antMatchers("/export/TO*").hasRole("TO_EXPORT_USER")
                        .antMatchers("/export/BF*").hasRole("BF_EXPORT_USER")
                        .antMatchers("/export/POGR*").hasRole("POGR_EXPORT_USER")
                        .antMatchers("/export/somu/FOI*").hasRole("FOI_EXPORT_USER")
                        .antMatchers("/export/somu/MPAM*").hasRole("MPAM_EXPORT_USER")
                        .antMatchers("/export/somu/COMP*").hasRole("COMP_EXPORT_USER")
                        .antMatchers("/export/somu/TO*").hasRole("TO_EXPORT_USER")
                        .antMatchers("/export/somu/POGR*").hasRole("POGR_EXPORT_USER")
                        .antMatchers("/export/topics*").hasAnyRole("DCU_EXPORT_USER", "FOI_EXPORT_USER")
                        .antMatchers("/export/teams*").hasAnyRole("DCU_EXPORT_USER", "WCS_EXPORT_USER",
                                "MPAM_EXPORT_USER", "COMP_EXPORT_USER", "FOI_EXPORT_USER", "IEDET_EXPORT_USER",
                                "BF_EXPORT_USER", "POGR_EXPORT_USER")
                        .antMatchers("/export/users*").hasAnyRole("DCU_EXPORT_USER", "WCS_EXPORT_USER",
                                "MPAM_EXPORT_USER", "COMP_EXPORT_USER", "FOI_EXPORT_USER", "IEDET_EXPORT_USER",
                                "BF_EXPORT_USER", "POGR_EXPORT_USER")
                        .anyRequest()
                        .authenticated()).oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter());
        return http.build();
    }

    JwtAuthenticationConverter jwtAuthenticationConverter() {
        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return jwtAuthenticationConverter;
    }
}
