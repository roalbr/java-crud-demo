package com.example.crud.config;

import com.example.crud.model.AppUser;
import com.example.crud.model.Product;
import com.example.crud.repository.AppUserRepository;
import com.example.crud.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Inicializador de dados para ambiente de desenvolvimento.
 * Cria usuários e produtos de exemplo.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createUsers();
        }
        if (productRepository.count() == 0) {
            createProducts();
        }
    }

    private void createUsers() {
        AppUser admin = AppUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@example.com")
                .fullName("Administrador")
                .roles(Set.of("ADMIN", "USER"))
                .active(true)
                .build();
        userRepository.save(admin);

        AppUser user = AppUser.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .email("user@example.com")
                .fullName("Usuário Padrão")
                .roles(Set.of("USER"))
                .active(true)
                .build();
        userRepository.save(user);

        log.info("Usuários criados: admin/admin123, user/user123");
    }

    private void createProducts() {
        productRepository.save(Product.builder()
                .name("Notebook Dell Inspiron 15")
                .description("Notebook Dell com Intel i7, 16GB RAM, 512GB SSD")
                .price(new BigDecimal("4599.99"))
                .category("Eletrônicos")
                .sku("DELL-INS-15-001")
                .stockQuantity(50)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .name("Mouse Logitech MX Master 3")
                .description("Mouse sem fio ergonômico com sensor Darkfield")
                .price(new BigDecimal("499.90"))
                .category("Periféricos")
                .sku("LOG-MXM3-001")
                .stockQuantity(200)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .name("Teclado Mecânico Keychron K2")
                .description("Teclado mecânico wireless 75% com switches Gateron")
                .price(new BigDecimal("699.00"))
                .category("Periféricos")
                .sku("KEY-K2-001")
                .stockQuantity(100)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .name("Monitor LG UltraWide 34\"")
                .description("Monitor ultrawide 34 polegadas QHD IPS")
                .price(new BigDecimal("3299.00"))
                .category("Eletrônicos")
                .sku("LG-UW34-001")
                .stockQuantity(30)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .name("Cadeira Gamer ThunderX3")
                .description("Cadeira gamer ergonômica com apoio lombar")
                .price(new BigDecimal("1899.90"))
                .category("Mobiliário")
                .sku("TX3-CHAIR-001")
                .stockQuantity(75)
                .active(true)
                .build());

        log.info("Produtos de exemplo criados");
    }
}
