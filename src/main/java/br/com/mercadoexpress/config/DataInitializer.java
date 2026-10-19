package br.com.mercadoexpress.config;

import br.com.mercadoexpress.model.AppUser;
import br.com.mercadoexpress.model.Product;
import br.com.mercadoexpress.model.UserRole;
import br.com.mercadoexpress.repository.AppUserRepository;
import br.com.mercadoexpress.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            ProductRepository products,
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.name:}") String adminName,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword
    ) {
        return args -> {
            if (products.count() == 0) {
                products.saveAll(List.of(
                        product("Morango orgânico", "Hortifruti", "Morango fresco, selecionado e direto do produtor.", "18.90", 24, "🍓"),
                        product("Pão de fermentação natural", "Padaria", "Casca crocante, miolo macio e fermentação lenta.", "16.50", 12, "🥖"),
                        product("Suco verde da casa", "Bebidas", "Maçã, couve, limão e gengibre. Feito hoje.", "12.00", 18, "🥤"),
                        product("Mel silvestre 300g", "Mercearia", "Mel puro de pequenos apicultores da região.", "27.90", 9, "🍯"),
                        product("Tomate italiano", "Hortifruti", "Tomates maduros para salada, molho ou bruschetta.", "8.75", 30, "🍅"),
                        product("Granola artesanal", "Mercearia", "Aveia, castanhas e frutas, sem conservantes.", "22.00", 14, "🥣")
                ));
            }
            if (!adminName.isBlank() && !adminEmail.isBlank() && !adminPassword.isBlank()
                    && !users.existsByEmailIgnoreCase(adminEmail)) {
                AppUser admin = new AppUser();
                admin.setFullName(adminName.trim());
                admin.setEmail(adminEmail.trim().toLowerCase(Locale.ROOT));
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.ADMIN);
                users.save(admin);
            }
        };
    }

    private Product product(String name, String category, String description, String price, int stock, String image) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setImageUrl(image);
        product.setActive(true);
        return product;
    }
}
