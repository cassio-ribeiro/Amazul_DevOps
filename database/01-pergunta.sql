-- Executar uma vez em schema sem PERGUNTA e sem SEQ_PERGUNTA.
-- Nunca remover objetos existentes automaticamente.
CREATE TABLE PERGUNTA (
    id_pergunta NUMBER(10) CONSTRAINT pk_pergunta PRIMARY KEY,
    enunciado VARCHAR2(500 CHAR) NOT NULL,
    resposta CHAR(1) NOT NULL CONSTRAINT ck_pergunta_resposta CHECK (resposta IN ('V','F')),
    explicacao VARCHAR2(1000 CHAR) NOT NULL,
    referencia VARCHAR2(300 CHAR)
);
CREATE SEQUENCE seq_pergunta START WITH 1 INCREMENT BY 1 NOCACHE;
