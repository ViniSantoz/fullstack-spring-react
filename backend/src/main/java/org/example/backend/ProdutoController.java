package org.example.backend;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    final private List<Produto> produtos = new ArrayList<>();

    @PostConstruct
    public void init() {
        produtos.add(new Produto(IdGenerator.nextId(Produto.class), "iPhone 14", 7999.0, 50, List.of("Eletrônicos", "Smartphones")));
        produtos.add(new Produto(IdGenerator.nextId(Produto.class), "Samsung Galaxy S23", 6999.0, 30, List.of("Eletrônicos", "Smartphones")));
        produtos.add(new Produto(IdGenerator.nextId(Produto.class), "Notebook Dell Inspiron", 4999.0, 20, List.of("Eletrônicos", "Computadores")));
    }

    @GetMapping
    public ResponseEntity<List<Produto>> buscarProdutos(
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "precoMinimo", required = false) Double precoMinimo,
            @RequestParam(name = "categoria", required = false) List<String> categorias,
            @RequestParam(name = "ordenarPor", defaultValue = "nome") String ordenarPor,
            @RequestParam(name = "ordem", defaultValue = "asc") String ordem,
            @RequestParam(name = "pagina", required = false) Integer pagina,
            @RequestParam(name = "tamanho", required = false) Integer tamanho) {

        // Aplicar filtros
        List<Produto> produtosFiltrados = filtrarProdutos(nome, precoMinimo, categorias);

        // Aplicar ordenação
        produtosFiltrados = ordenarProdutos(produtosFiltrados, ordenarPor, ordem);

        // Aplicar paginação, se necessário
        if (pagina != null && tamanho != null) {
            return aplicarPaginacao(produtosFiltrados, pagina, tamanho);
        }

        // Retornar lista filtrada e ordenada sem paginação
        return ResponseEntity.ok(produtosFiltrados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarProdutoById(@PathVariable Long id) {
        Produto produto = produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado com o ID: " + id));

        if (produto != null) {
            return ResponseEntity.ok(produto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Produto> criarProduto(@Valid @RequestBody Produto produto) {
        produto.setId(IdGenerator.nextId(Produto.class));
        produtos.add(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> exlcuirProduto(@PathVariable Long id) {
        Produto produto = produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado com o ID: " + id));

        produtos.remove(produto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizarProduto(@PathVariable Long id, @Valid @RequestBody Produto produtoAtualizado) {
        Produto produto = produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado com o ID: " + id));

        produto.setNome(produtoAtualizado.getNome());
        produto.setPreco(produtoAtualizado.getPreco());
        produto.setCategorias(produtoAtualizado.getCategorias());
        return new ResponseEntity<>(produto, HttpStatus.OK);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<Produto> atualizarParcialmenteProduto(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        Produto produto = produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado com o ID: " + id));

        fields.forEach((key, value) -> {
            if (value == null) {
                throw new IllegalArgumentException("O valor para o campo '" + key + "' não pode ser nulo.");
            }

            switch (key) {
                case "nome" -> {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException("O campo 'nome' deve ser uma String.");
                    }
                    produto.setNome((String) value);
                }
                case "preco" -> {
                    if (!(value instanceof Number)) {
                        throw new IllegalArgumentException("O campo 'preco' deve ser numérico.");
                    }
                    produto.setPreco(Double.valueOf(value.toString()));
                }
                case "categorias" -> {
                    if (!(value instanceof List<?>)) {
                        throw new IllegalArgumentException("O campo 'categorias' deve ser uma lista de Strings.");
                    }
                    List<String> categorias = ((List<?>) value).stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    produto.setCategorias(categorias);
                }
                default -> throw new IllegalArgumentException("Campo inválido: " + key);
            }
        });

        return ResponseEntity.ok(produto);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    private List<Produto> filtrarProdutos(String nome, Double precoMinimo, List<String> categorias) {
        return produtos.stream()
                .filter(p -> nome == null || p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .filter(p -> precoMinimo == null || p.getPreco() >= precoMinimo)
                .filter(p -> categorias == null || categorias.stream()
                        .anyMatch(categoria -> p.getCategorias().stream()
                                .anyMatch(prodCategoria -> prodCategoria.equalsIgnoreCase(categoria))))
                .collect(Collectors.toList());
    }

    private List<Produto> ordenarProdutos(List<Produto> produtos, String ordenarPor, String ordem) {
        return produtos.stream()
                .sorted((p1, p2) -> {
                    int comparison = switch (ordenarPor) {
                        case "id" -> Long.compare(p1.getId(), p2.getId());
                        case "nome" -> p1.getNome().compareToIgnoreCase(p2.getNome());
                        case "preco" -> Double.compare(p1.getPreco(), p2.getPreco());
                        case "estoque" -> Integer.compare(p1.getEstoque(), p2.getEstoque());
                        default -> 0;
                    };
                    return "desc".equalsIgnoreCase(ordem) ? -comparison : comparison;
                })
                .collect(Collectors.toList());
    }

    private ResponseEntity<List<Produto>> aplicarPaginacao(List<Produto> produtos, int pagina, int tamanho) {
        final String HEADER_TOTAL_COUNT = "X-Total-Count";
        final String HEADER_TOTAL_PAGES = "X-Total-Pages";
        final String HEADER_PAGE = "X-Page";
        final String HEADER_PAGE_SIZE = "X-Page-Size";

        int totalRegistros = produtos.size();
        int totalPaginas = (int) Math.ceil((double) totalRegistros / tamanho);
        int inicio = pagina * tamanho;
        int fim = Math.min(inicio + tamanho, totalRegistros);

        List<Produto> paginaDeProdutos = inicio < totalRegistros
                ? produtos.subList(inicio, fim)
                : List.of();

        return ResponseEntity.ok()
                .header(HEADER_TOTAL_COUNT, String.valueOf(totalRegistros))
                .header(HEADER_TOTAL_PAGES, String.valueOf(totalPaginas))
                .header(HEADER_PAGE, String.valueOf(pagina))
                .header(HEADER_PAGE_SIZE, String.valueOf(tamanho))
                .body(paginaDeProdutos);
    }
}
