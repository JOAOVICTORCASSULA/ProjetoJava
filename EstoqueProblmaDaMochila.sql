CREATE DATABASE EstoqueProblemaMochila;
USE EstoqueProblemaMochila;

CREATE TABLE estoque (
	id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
    capacidadeTotal INTEGER NOT NULL CHECK (capacidadeTotal > 0)
);

CREATE TABLE produto (
    id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
    peso DOUBLE CHECK (peso > 0),
    valor DOUBLE CHECK (valor > 0),
    nome VARCHAR(250) NOT NULL,
    CONSTRAINT estoque_produto_FK FOREIGN KEY(id)
    REFERENCES estoque(id)
);



