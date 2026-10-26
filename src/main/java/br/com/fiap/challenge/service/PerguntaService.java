package br.com.fiap.challenge.service;

import br.com.fiap.challenge.dto.*;
import br.com.fiap.challenge.entity.Pergunta;
import br.com.fiap.challenge.exception.RecursoNaoEncontradoException;
import br.com.fiap.challenge.repository.PerguntaOracleRepository;
import br.com.fiap.challenge.repository.PerguntaRepository;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class PerguntaService {
    private static final Set<String> CAMPOS_ORDENACAO =
            Set.of("id", "enunciado", "resposta", "explicacao", "referencia");
    private final PerguntaRepository repository;
    private final Optional<PerguntaOracleRepository> oracle;

    public PerguntaService(
            PerguntaRepository repository, Optional<PerguntaOracleRepository> oracle) {
        this.repository = repository;
        this.oracle = oracle;
    }

    @Transactional
    public Pergunta cadastrar(PerguntaDTO dto) {
        Pergunta pergunta = new Pergunta();
        preencher(pergunta, dto);
        return oracle.isPresent() ? oracle.get().inserir(pergunta) : repository.save(pergunta);
    }

    public Page<Pergunta> listar(String texto, Pageable pageable) {
        if (pageable.getSort().stream()
                .anyMatch(o -> !CAMPOS_ORDENACAO.contains(o.getProperty()))) {
            throw new IllegalArgumentException("Campo de ordenacao invalido");
        }
        return texto == null || texto.isBlank()
                ? repository.findAll(pageable)
                : repository.findByEnunciadoContainingIgnoreCase(texto.trim(), pageable);
    }

    public Pergunta buscarPorId(Long id) {
        return repository
                .findById(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Pergunta nao encontrada: " + id));
    }

    @Transactional
    public Pergunta atualizar(Long id, PerguntaDTO dto) {
        Pergunta pergunta = buscarPorId(id);
        if (oracle.isPresent()) {
            Pergunta valores = new Pergunta();
            preencher(valores, dto);
            return oracle.get().atualizar(id, valores);
        }
        preencher(pergunta, dto);
        return repository.save(pergunta);
    }

    @Transactional
    public void excluir(Long id) {
        Pergunta p = buscarPorId(id);
        if (oracle.isPresent()) oracle.get().excluir(id);
        else repository.delete(p);
    }

    public CartaoDTO sortear() {
        // Sorteia uma posicao, nao um ID: continua funcionando depois de exclusoes.
        long total = repository.count();
        if (total == 0) throw new RecursoNaoEncontradoException("Nao ha perguntas cadastradas");
        int indice = ThreadLocalRandom.current().nextInt(Math.toIntExact(total));
        Page<Pergunta> pagina = repository.findAll(PageRequest.of(indice, 1, Sort.by("id")));
        if (pagina.isEmpty())
            throw new RecursoNaoEncontradoException("O acervo mudou; tente novamente");
        Pergunta p = pagina.getContent().getFirst();
        return new CartaoDTO(p.getId(), p.getEnunciado());
    }

    public ResultadoDTO responder(Long id, RespostaDTO dto) {
        Pergunta p = buscarPorId(id);
        return new ResultadoDTO(
                p.getId(),
                p.getResposta().equals(dto.getResposta()),
                p.getResposta(),
                p.getExplicacao(),
                p.getReferencia());
    }

    private void preencher(Pergunta p, PerguntaDTO dto) {
        p.setEnunciado(dto.getEnunciado().trim());
        p.setResposta(dto.getResposta());
        p.setExplicacao(dto.getExplicacao().trim());
        p.setReferencia(
                dto.getReferencia() == null || dto.getReferencia().isBlank()
                        ? null
                        : dto.getReferencia().trim());
    }
}
