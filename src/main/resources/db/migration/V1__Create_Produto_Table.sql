CREATE TABLE produto (
     id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
     codigo VARCHAR(255) NOT NULL UNIQUE,
     descricao VARCHAR(255) NOT NULL,
     tipo VARCHAR(255) NOT NULL,
     valor_fornecedor DECIMAL(19, 2) NOT NULL,
     quantidade_estoque INTEGER NOT NULL
);