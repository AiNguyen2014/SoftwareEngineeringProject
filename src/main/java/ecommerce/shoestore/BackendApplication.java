package ecommerce.shoestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Main Spring Boot Application Class
 * Entry point của application
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);

        System.out.println("\n" +
                "   ➜ Local:   http://localhost:8080                       \n" +
                "   ➜ API:     http://localhost:8080/api                   \n");
    }
}