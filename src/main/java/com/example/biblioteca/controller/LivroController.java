package com.example.biblioteca.controller;

import com.example.biblioteca.exception.NotFoundException;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroRepository livroRepository;
    public LivroController(LivroRepository livroRepository) { this.livroRepository = livroRepository; }

    @PostMapping
    public ResponseEntity<?> criarLivro(@Valid @RequestBody Livro novoLivro) {
        if(livroRepository.findByIsbn(novoLivro.getIsbn()).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ISBN já existe");
        return ResponseEntity.status(HttpStatus.CREATED).body(livroRepository.save(novoLivro));
    }

    @GetMapping
    public List<Livro> listarTodos() { return livroRepository.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> obterPorId(@PathVariable Long id) {
        return livroRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Livro não encontrado"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarLivro(@PathVariable Long id, @Valid @RequestBody Livro dto) {
        return livroRepository.findById(id).map(existing -> {
            if(!existing.getIsbn().equals(dto.getIsbn()) && livroRepository.findByIsbn(dto.getIsbn()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body("ISBN já cadastrado");
            existing.setTitulo(dto.getTitulo());
            existing.setAutor(dto.getAutor());
            existing.setIsbn(dto.getIsbn());
            existing.setAnoPublicacao(dto.getAnoPublicacao());
            existing.setDisponivel(dto.getDisponivel());
            return ResponseEntity.ok(livroRepository.save(existing));
        }).orElseThrow(() -> new NotFoundException("Livro não encontrado"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchLivro(@PathVariable Long id, @RequestBody Livro patch) {
        return livroRepository.findById(id).map(existing -> {
            if(patch.getTitulo() != null) existing.setTitulo(patch.getTitulo());
            if(patch.getAutor() != null) existing.setAutor(patch.getAutor());
            if(patch.getIsbn() != null && !patch.getIsbn().equals(existing.getIsbn()) && livroRepository.findByIsbn(patch.getIsbn()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body("ISBN já cadastrado");
            if(patch.getIsbn() != null) existing.setIsbn(patch.getIsbn());
            if(patch.getAnoPublicacao() != null) existing.setAnoPublicacao(patch.getAnoPublicacao());
            if(patch.getDisponivel() != null) existing.setDisponivel(patch.getDisponivel());
            return ResponseEntity.ok(livroRepository.save(existing));
        }).orElseThrow(() -> new NotFoundException("Livro não encontrado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return livroRepository.findById(id).map(existing -> {
            livroRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }).orElseThrow(() -> new NotFoundException("Livro não encontrado"));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
