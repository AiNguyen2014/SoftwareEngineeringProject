package ecommerce.shoestore.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Class
 * Entry point của application
 */
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);

        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════════╗\n" +
                "║                                                           ║\n" +
                "║           🎉 WebShoe Application Started! 🎉             ║\n" +
                "║                                                           ║\n" +
                "║   ➜ Local:   http://localhost:8080                       ║\n" +
                "║   ➜ API:     http://localhost:8080/api                   ║\n" +
                "║                                                           ║\n" +
                "║   Use Cases Implemented:                                 ║\n" +
                "║   ✓ View Product List (/)                                ║\n" +
                "║   ✓ View Product Detail (/product/{id})                 ║\n" +
                "║                                                           ║\n" +
                "╚═══════════════════════════════════════════════════════════╝\n");
    }
}