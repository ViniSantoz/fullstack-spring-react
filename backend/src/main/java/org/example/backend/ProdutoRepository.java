package org.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByPrecoLessThanEqual(Double preco);
    List<Produto> findByEstoqueGreaterThan(Integer estoque);

    // Consultas relacionadas aos relacionamentos
    List<Produto> findByCategoria(Categoria categoria);
    List<Produto> findByFornecedoresContains(Fornecedor fornecedor);
}
