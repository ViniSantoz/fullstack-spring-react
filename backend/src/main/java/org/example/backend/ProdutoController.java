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

    // Lista em memória para simular o banco de dados para armazenar os produtos.
    final private List<Produto> produtos = new ArrayList<>();

    /**
     * Metodo de inicialização utilizado para popular a lista de produtos, somente para testes ou simulação.
     * Esse metodo é executado automaticamente após a construção da classe (@PostConstruct).
     */
    @PostConstruct
    public void init() {
        produtos.add(new Produto(IdGenerator.nextId(Produto.class), "iPhone 14", 7999.0, 50, List.of("Eletrônicos", "Smartphones")));
        produtos.add(new Produto(IdGenerator.nextId(Produto.class), "Samsung Galaxy S23", 6999.0, 30, List.of("Eletrônicos", "Smartphones")));
        produtos.add(new Produto(IdGenerator.nextId(Produto.class), "Notebook Dell Inspiron", 4999.0, 20, List.of("Eletrônicos", "Computadores")));
    }

    /**
     * Endpoint para buscar produtos. Suporta filtros, ordenação e paginação configuráveis por parâmetros de requisição.
     *
     * @param nome         Filtro para buscar produtos pelo nome (opcional).
     * @param precoMinimo  Filtro para buscar produtos com preço maior ou igual a um valor especificado (opcional).
     * @param categorias   Lista de categorias a serem usadas como filtro (opcional).
     * @param ordenarPor   Campo pelo qual os produtos serão ordenados (opcional, padrão: "nome").
     * @param ordem        Ordem da lista: ascendente (asc) ou descendente (desc) (opcional, padrão: "asc").
     * @param pagina       Número da página para paginação (opcional).
     * @param tamanho      Tamanho da página (quantidade de itens por página) (opcional).
     * @return Lista de produtos filtrada, ordenada e paginada.
     */
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

    /**
     * Busca um único produto pelo seu identificador (ID).
     *
     * @param id O ID do produto a ser buscado.
     * @return Produto encontrado ou erro 404 caso o produto não exista.
     */
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

    /**
     * Cria um novo produto a partir das informações fornecidas no corpo da requisição (JSON).
     *
     * @param produto O objeto Produto enviado no corpo da requisição (deve ser válido).
     * @return O produto criado, com código HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Produto> criarProduto(@Valid @RequestBody Produto produto) {
        produto.setId(IdGenerator.nextId(Produto.class));
        produtos.add(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    /**
     * Exclui um produto existente pelo seu identificador (ID).
     *
     * @param id O ID do produto a ser excluído.
     * @return Código HTTP 204 (No Content) se a exclusão for bem-sucedida.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> exlcuirProduto(@PathVariable Long id) {
        Produto produto = produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado com o ID: " + id));

        produtos.remove(produto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Atualiza completamente os dados de um produto existente pelo ID.
     *
     * @param id               O ID do produto que será atualizado.
     * @param produtoAtualizado O objeto Produto atualizado enviado no corpo da requisição.
     * @return O produto atualizado ou erro 404 caso o produto não exista.
     */
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

    /**
     * Atualiza parcialmente os dados de um produto existente pelo ID.
     *
     * @param id     O ID do produto a ser atualizado.
     * @param fields Um mapa contendo os campos e valores a serem atualizados.
     * @return O produto parcialmente atualizado ou erro 404 caso o produto não exista.
     */
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

    /**
     * Exceção personalizada para lidar com casos onde um produto não é encontrado.
     * Esse metodo é chamado automaticamente pelo Spring em exceções do tipo NoSuchElementException.
     *
     * @param ex A exceção capturada (detalhes do erro).
     * @return Mensagem de erro com código HTTP 404 (Not Found).
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Filtra produtos da lista com base nos critérios especificados (nome, preço mínimo, categorias).
     * Este metodo é usado internamente por `buscarProdutos()`.
     *
     * @param nome        Nome do produto (opcional).
     * @param precoMinimo Preço mínimo (opcional).
     * @param categorias  Categorias desejadas (opcional).
     * @return Uma lista filtrada de produtos.
     */
    private List<Produto> filtrarProdutos(String nome, Double precoMinimo, List<String> categorias) {
        return produtos.stream()
                .filter(p -> nome == null || p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .filter(p -> precoMinimo == null || p.getPreco() >= precoMinimo)
                .filter(p -> categorias == null || categorias.stream()
                        .anyMatch(categoria -> p.getCategorias().stream()
                                .anyMatch(prodCategoria -> prodCategoria.equalsIgnoreCase(categoria))))
                .collect(Collectors.toList());
    }

    /**
     * Ordena uma lista de produtos com base em um campo e ordem (ascendente/descendente).
     * Este metodo é usado internamente por `buscarProdutos()`.
     *
     * @param produtos   Lista de produtos a ser ordenada.
     * @param ordenarPor Campo de ordenação (ex.: nome, preço).
     * @param ordem      Ordem da ordenação: ascendente (asc) ou descendente (desc).
     * @return Uma lista ordenada de produtos.
     */
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

    /**
     * Aplica paginação a uma lista de produtos.
     * Este metodo é usado internamente por `buscarProdutos()`.
     *
     * @param produtos Lista de produtos a ser paginada.
     * @param pagina   Número da página.
     * @param tamanho  Tamanho da página.
     * @return Uma resposta paginada contendo apenas o subconjunto solicitado.
     */
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
