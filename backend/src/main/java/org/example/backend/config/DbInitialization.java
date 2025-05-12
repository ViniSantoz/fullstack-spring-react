package org.example.backend.config;

import org.example.backend.Produto;
import org.example.backend.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DbInitialization {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Bean
    public CommandLineRunner inicializarDados() {
        return args -> {
            // Inicializar produtos na inicialização da aplicação
            produtoRepository.saveAll(List.of(
                    new Produto(
                            "iPhone 14",
                            7999.0,
                            50
                    ),
                    new Produto(
                            "Samsung Galaxy S23",
                            6999.0,
                            30
                    ),
                    new Produto(
                            "Notebook Dell Inspiron",
                            4999.0,
                            20
                    )
            ));
        };
    }
}