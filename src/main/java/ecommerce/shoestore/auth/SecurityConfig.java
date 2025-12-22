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

            .authorizeHttpRequests(auth -> auth
                // PUBLIC
                .requestMatchers(
                    "/", "/index",
                    "/auth/**",
                    "/css/**", "/js/**", "/images/**",
                    "/error",
                    "/product/**",
                    "/user/**"
                ).permitAll()

                // ADMIN ONLY
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // CÒN LẠI: CẦN LOGIN
                .anyRequest().authenticated()
            )

            // QUAN TRỌNG: TẮT formLogin
            .formLogin(form -> form.disable())

            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
