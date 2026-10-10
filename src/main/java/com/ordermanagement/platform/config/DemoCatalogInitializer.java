
package com.ordermanagement.platform.config;

import com.ordermanagement.platform.category.entity.Category;
import com.ordermanagement.platform.category.repository.CategoryRepository;
import com.ordermanagement.platform.product.entity.Product;
import com.ordermanagement.platform.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data.enabled",
        havingValue = "true")
public class DemoCatalogInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {

        Category electronics = getOrCreateCategory(
                "Electrónica",
                "Productos electrónicos y tecnológicos"
        );

        Category office = getOrCreateCategory(
                "Oficina",
                "Materiales y accesorios para oficina y trabajo"
        );

        Category accessories = getOrCreateCategory(
                "Accesorios",
                "Accesorios y complementos para equipos tecnológicos"
        );

        createProductIfMissing(
                "Laptop Lenovo ThinkPad",
                "Laptop empresarial para productividad y trabajo profesional",
                "2499.90",
                25,
                electronics
        );

        createProductIfMissing(
                "Monitor Dell 24 pulgadas",
                "Monitor Full HD para oficina y trabajo diario",
                "649.90",
                30,
                electronics
        );

        createProductIfMissing(
                "Teclado mecánico",
                "Teclado mecánico para programación y escritura",
                "189.90",
                40,
                electronics
        );

        createProductIfMissing(
                "Mouse inalámbrico",
                "Mouse inalámbrico ergonómico para uso diario",
                "79.90",
                50,
                accessories
        );

        createProductIfMissing(
                "Hub USB-C",
                "Adaptador multipuerto para conectar periféricos",
                "119.90",
                20,
                accessories
        );

        createProductIfMissing(
                "Cuaderno de oficina",
                "Cuaderno para notas y organización de tareas",
                "12.90",
                100,
                office
        );

        System.out.println(
                "Catálogo de demostración verificado correctamente."
        );
    }

    private Category getOrCreateCategory(
            String name,
            String description
    ) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name(name)
                                .description(description)
                                .active(true)
                                .build()
                ));
    }

    private void createProductIfMissing(
            String name,
            String description,
            String price,
            int stock,
            Category category
    ) {
        if (productRepository.existsByNameIgnoreCase(name)) {
            return;
        }

        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .stock(stock)
                .active(true)
                .category(category)
                .build();

        productRepository.save(product);
    }
}