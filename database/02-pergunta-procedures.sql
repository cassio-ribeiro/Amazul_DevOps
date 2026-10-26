CREATE OR REPLACE PROCEDURE pr_inserir_pergunta (
    p_enunciado IN VARCHAR2, p_resposta IN CHAR, p_explicacao IN VARCHAR2,
    p_referencia IN VARCHAR2, p_id OUT NUMBER
) AS
BEGIN
    p_id := seq_pergunta.NEXTVAL;
    INSERT INTO pergunta (id_pergunta, enunciado, resposta, explicacao, referencia)
    VALUES (p_id, p_enunciado, p_resposta, p_explicacao, p_referencia);
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20001, 'Nao foi possivel inserir a pergunta: ' || SQLERRM);
END;
/
CREATE OR REPLACE PROCEDURE pr_atualizar_pergunta (
    p_id IN NUMBER, p_enunciado IN VARCHAR2, p_resposta IN CHAR,
    p_explicacao IN VARCHAR2, p_referencia IN VARCHAR2
) AS
    v_linhas NUMBER;
BEGIN
    UPDATE pergunta SET enunciado = p_enunciado, resposta = p_resposta,
        explicacao = p_explicacao, referencia = p_referencia WHERE id_pergunta = p_id;
    v_linhas := SQL%ROWCOUNT;
    IF v_linhas = 0 THEN RAISE_APPLICATION_ERROR(-20002, 'Pergunta inexistente'); END IF;
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20003, 'Nao foi possivel atualizar a pergunta: ' || SQLERRM);
END;
/
CREATE OR REPLACE PROCEDURE pr_excluir_pergunta (p_id IN NUMBER) AS
    v_linhas NUMBER;
BEGIN
    DELETE FROM pergunta WHERE id_pergunta = p_id;
    v_linhas := SQL%ROWCOUNT;
    IF v_linhas = 0 THEN RAISE_APPLICATION_ERROR(-20004, 'Pergunta inexistente'); END IF;
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20005, 'Nao foi possivel excluir a pergunta: ' || SQLERRM);
END;
/
-- Sem COMMIT nas procedures: a transacao e controlada pela API.
