package br.com.fiap.challenge.repository;

import br.com.fiap.challenge.entity.Pergunta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {
    Page<Pergunta> findByEnunciadoContainingIgnoreCase(String texto, Pageable pageable);
}
