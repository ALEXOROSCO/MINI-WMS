package com.miniwms;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoRepository repository;

    public ProdutoController(ProdutoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Produto> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Produto cadastrar(@RequestBody Produto produto) {
        if (produto.getQuantidade() == null) {
            produto.setQuantidade(0);
        }
        return repository.save(produto);
    }

    @PostMapping("/{id}/movimentar")
    public ResponseEntity<?> movimentar(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Produto produto = repository.findById(id).orElse(null);
        if (produto == null) {
            return ResponseEntity.notFound().build();
        }

        Object tipoPayload = payload.get("tipo");
        Object quantidadePayload = payload.get("quantidade");
        if (!(tipoPayload instanceof String) || quantidadePayload == null) {
            return ResponseEntity.badRequest().body("Informe o tipo e a quantidade da movimentação.");
        }

        String tipo = (String) tipoPayload;
        Integer qtd;
        try {
            qtd = Integer.valueOf(quantidadePayload.toString());
        } catch (NumberFormatException exception) {
            return ResponseEntity.badRequest().body("A quantidade deve ser um número inteiro positivo.");
        }

        if (qtd <= 0) {
            return ResponseEntity.badRequest().body("A quantidade deve ser um número inteiro positivo.");
        }

        if ("SAIDA".equalsIgnoreCase(tipo)) {
            if (produto.getQuantidade() == null || produto.getQuantidade() < qtd) {
                return ResponseEntity.badRequest().body("Estoque insuficiente para a saída solicitada.");
            }
            produto.setQuantidade(produto.getQuantidade() - qtd);
        } else if ("ENTRADA".equalsIgnoreCase(tipo)) {
            int quantidadeAtual = produto.getQuantidade() == null ? 0 : produto.getQuantidade();
            produto.setQuantidade(quantidadeAtual + qtd);
        } else {
            return ResponseEntity.badRequest().body("Tipo de movimentação inválido (use ENTRADA ou SAIDA).");
        }

        repository.save(produto);
        return ResponseEntity.ok(produto);
    }
}