package org.example.backend.config;

import org.example.backend.Categoria;
import org.example.backend.CategoriaRepository;
import org.example.backend.Produto;
import org.example.backend.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DbInitialization {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public DbInitialization(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Bean
    public CommandLineRunner inicializarDados() {
        return args -> {
            Categoria smartphones = new Categoria("Smartphones");
            Categoria notebooks = new Categoria("Notebooks");

            smartphones = categoriaRepository.save(smartphones);
            notebooks = categoriaRepository.save(notebooks);

            produtoRepository.saveAll(List.of(
                    new Produto(
                            "iPhone 14",
                            7999.0,
                            50,
                            smartphones
                    ),
                    new Produto(
                            "Samsung Galaxy S23",
                            6999.0,
                            30,
                            smartphones
                    ),
                    new Produto(
                            "Notebook Dell Inspiron",
                            4999.0,
                            20,
                            notebooks
                    )
            ));
        };
    }
}