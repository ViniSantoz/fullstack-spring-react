package org.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Spring Data JPA fornece métodos básicos: save(), findById(), findAll(), delete(), etc.

    // Exemplos de consultas derivadas
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByPrecoLessThanEqual(Double preco);

    List<Produto> findByEstoqueGreaterThan(Integer estoque);
}
