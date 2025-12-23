package ecommerce.shoestore.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session
                        -> session.sessionFixation().none()
                )
                .authorizeHttpRequests(auth -> auth
                // PUBLIC
                .requestMatchers(
                        "/", "/index", "/shoes", "/product/**",
                        "/auth/**", "/user/**", "/cart/**",
                        "/css/**", "/js/**", "/images/**",
                        "/error"
                ).permitAll()
                // ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // CÒN LẠI PHẢI LOGIN
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/j_spring_security_check")
                .defaultSuccessUrl("/", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
                );

        return http.build();
    }
}
