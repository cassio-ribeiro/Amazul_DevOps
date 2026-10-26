package br.com.fiap.challenge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PERGUNTA")
public class Pergunta {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pergunta_seq")
    @SequenceGenerator(name = "pergunta_seq", sequenceName = "seq_pergunta", allocationSize = 1)
    @Column(name = "id_pergunta", precision = 10)
    private Long id;

    @Column(nullable = false, length = 500)
    private String enunciado;

    @Column(nullable = false, length = 1, columnDefinition = "char(1)")
    private String resposta;

    @Column(nullable = false, length = 1000)
    private String explicacao;

    @Column(length = 300)
    private String referencia;

    public Pergunta() {}

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String value) {
        enunciado = value;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String value) {
        resposta = value;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public void setExplicacao(String value) {
        explicacao = value;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String value) {
        referencia = value;
    }
}
