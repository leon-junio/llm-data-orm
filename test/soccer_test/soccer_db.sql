USE `soccer_test`;

CREATE TABLE soccer_br (
    id INT AUTO_INCREMENT PRIMARY KEY,
    equipe VARCHAR(100) NOT NULL,
    posicao INT NOT NULL,
    pontos INT NOT NULL,
    jogos INT NOT NULL,
    vitorias INT NOT NULL,
    empates INT NOT NULL,
    derrotas INT NOT NULL,
    gols_pro INT NOT NULL,
    gols_contra INT NOT NULL,
    saldo_gols INT NOT NULL,
    classificacao_final VARCHAR(255),
    competicao VARCHAR(100) NOT NULL,
    observacoes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);