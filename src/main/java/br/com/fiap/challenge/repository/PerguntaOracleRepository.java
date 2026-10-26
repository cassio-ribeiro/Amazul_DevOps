package br.com.fiap.challenge.repository;

import br.com.fiap.challenge.entity.Pergunta;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
@Profile("oracle")
public class PerguntaOracleRepository {
    private final JdbcTemplate jdbc;
    private final PerguntaRepository perguntas;

    public PerguntaOracleRepository(JdbcTemplate jdbc, PerguntaRepository perguntas) {
        this.jdbc = jdbc;
        this.perguntas = perguntas;
    }

    public Pergunta inserir(Pergunta p) {
        Long id =
                jdbc.execute(
                        (ConnectionCallback<Long>)
                                connection -> {
                                    try (var call =
                                            connection.prepareCall(
                                                    "{call pr_inserir_pergunta(?, ?, ?, ?, ?)}")) {
                                        call.setString(1, p.getEnunciado());
                                        call.setString(2, p.getResposta());
                                        call.setString(3, p.getExplicacao());
                                        call.setString(4, p.getReferencia());
                                        call.registerOutParameter(5, Types.NUMERIC);
                                        call.execute();
                                        return call.getLong(5);
                                    }
                                });
        return perguntas.findById(id).orElseThrow();
    }

    public Pergunta atualizar(Long id, Pergunta valores) {
        jdbc.execute(
                (ConnectionCallback<Void>)
                        connection -> {
                            try (var call =
                                    connection.prepareCall(
                                            "{call pr_atualizar_pergunta(?, ?, ?, ?, ?)}")) {
                                call.setLong(1, id);
                                call.setString(2, valores.getEnunciado());
                                call.setString(3, valores.getResposta());
                                call.setString(4, valores.getExplicacao());
                                call.setString(5, valores.getReferencia());
                                call.execute();
                                return null;
                            }
                        });
        // Le a linha atualizada via JDBC sem alterar a entidade gerenciada pelo JPA.
        // Assim o dirty checking nao executa outro UPDATE fora da procedure.
        return jdbc.queryForObject(
                "SELECT id_pergunta, enunciado, resposta, explicacao, referencia FROM pergunta"
                        + " WHERE id_pergunta = ?",
                (rs, row) -> {
                    Pergunta p = new Pergunta();
                    p.setId(rs.getLong(1));
                    p.setEnunciado(rs.getString(2));
                    p.setResposta(rs.getString(3));
                    p.setExplicacao(rs.getString(4));
                    p.setReferencia(rs.getString(5));
                    return p;
                },
                id);
    }

    public void excluir(Long id) {
        jdbc.execute(
                (ConnectionCallback<Void>)
                        connection -> {
                            try (var call =
                                    connection.prepareCall("{call pr_excluir_pergunta(?)}")) {
                                call.setLong(1, id);
                                call.execute();
                                return null;
                            }
                        });
    }
}
