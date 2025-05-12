package org.example.backend;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com id: " + id));
    }

    @Transactional
    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, Produto produtoAtualizado) {
        // Verifica se o produto existe
        Produto produtoExistente = buscarPorId(id);

        // Atualiza os campos
        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        produtoExistente.setEstoque(produtoAtualizado.getEstoque());

        // Salva no banco de dados
        return produtoRepository.save(produtoExistente);
    }

    @Transactional
    public void deletar(Long id) {
        // Verifica se o produto existe
        buscarPorId(id);
        produtoRepository.deleteById(id);
    }

    // Métodos usando as consultas derivadas do repositório
    @Transactional(readOnly = true)
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarPorPrecoMaximo(Double preco) {
        return produtoRepository.findByPrecoLessThanEqual(preco);
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarPorEstoqueMinimo(Integer estoque) {
        return produtoRepository.findByEstoqueGreaterThan(estoque);
    }
}
