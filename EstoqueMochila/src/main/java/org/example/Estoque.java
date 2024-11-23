package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Estoque {
    int estoque_id;
    private static int contadorIds = 1;
    private final double capacidadeTotal;
    private final List<Produto> produtos;

    public Estoque(double capacidadeTotal) {
        this.estoque_id = contadorIds++;
        this.capacidadeTotal = capacidadeTotal;
        this.produtos = new ArrayList<>();
    }

    public double getCapacidadeTotal() {
        return capacidadeTotal;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    protected void removerProdutoDoBanco(int produtoId) {
        String sql = "DELETE FROM produto WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Produto removido com sucesso do banco de dados.");
            } else {
                System.out.println("Falha ao remover o produto ou produto não encontrado.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao remover produto: " + e.getMessage());
        }
    }

    protected Estoque buscarEstoquePorId(int estoqueId) {
        String sql = "SELECT * FROM estoque WHERE id = ?";
        Estoque estoque = null;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estoqueId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double capacidadeTotal = rs.getDouble("capacidadeTotal");
                estoque = new Estoque(capacidadeTotal);
                estoque.estoque_id = estoqueId;  // Define o ID do estoque
                estoque.produtos.addAll(listarProdutosDoBanco(estoqueId));  // Carrega os produtos
            } else {
                System.out.println("Estoque não encontrado com o ID: " + estoqueId);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar estoque: " + e.getMessage());
        }

        return estoque;
    }

    private double calcularPesoTotalAtual() {
        double pesoTotal = 0.0;
        for (Produto produto : produtos) {
            pesoTotal += produto.getPeso();
        }
        return pesoTotal;
    }

    private double calcularPesoTotalNoBanco(int idEstoque) {
        String sql = "SELECT SUM(peso) AS peso_total FROM produto WHERE estoque_id = ?";
        double pesoTotal = 0.0;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEstoque);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pesoTotal = rs.getDouble("peso_total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pesoTotal;
    }



    protected List<Produto> listarProdutosDoBanco(int estoque_id) {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto WHERE estoque_id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estoque_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Produto produto = new Produto(
                        rs.getDouble("peso"),
                        rs.getDouble("valor"),
                        rs.getString("nome")
                );
                produto.setId(rs.getInt("id")); // Seta o ID do banco no objeto Produto
                produtos.add(produto);

                // Adiciona uma mensagem de debug para conferir os dados
                System.out.println("Produto ID: " + produto.getId() +
                        ", Nome: " + produto.getNome() +
                        ", Peso: " + produto.getPeso() +
                        ", Valor: " + produto.getValor());
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
        }
        return produtos;
    }

    public void otimizarDistribuicao(double capacidadeMochila) {
        // Verificando se a capacidade da mochila é válida
        if (capacidadeMochila <= 0) {
            System.out.println("A capacidade deve ser um número positivo.");
            return;
        }

        // Obtenção dos produtos
        List<Produto> produtos = this.getProdutos(); // Método que retorna os produtos do estoque
        int n = produtos.size();

        // Criando a tabela para otimização (Knapsack)
        double[][] dp = new double[n + 1][(int) capacidadeMochila + 1];

        // Preenchendo a tabela dp
        for (int i = 1; i <= n; i++) {
            Produto produto = produtos.get(i - 1);
            int pesoInt = (int) Math.round(produto.getPeso());
            double valor = produto.getValor();

            if (pesoInt <= 0) {
                continue;
            }

            for (int j = 0; j <= capacidadeMochila; j++) {
                if (pesoInt <= j) {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - pesoInt] + valor);
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }

        // Recuperando os itens escolhidos
        int w = (int) capacidadeMochila;
        StringBuilder produtosEscolhidos = new StringBuilder();
        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                Produto produto = produtos.get(i - 1);
                produtosEscolhidos.append(produto.getNome())
                        .append(" (Peso: ").append(produto.getPeso())
                        .append(", Valor: ").append(produto.getValor())
                        .append(")\n");
                w -= (int) Math.round(produto.getPeso());
            }
        }

        // Exibindo os resultados
        if (produtosEscolhidos.length() > 0) {
            System.out.println("Produtos escolhidos para carregar na mochila:\n" + produtosEscolhidos.toString());
        } else {
            System.out.println("Nenhum produto foi selecionado.");
        }
    }

    public int adicionarProdutoNoBanco(Produto produto, int idEstoque) {
        double pesoTotalBanco = calcularPesoTotalNoBanco(idEstoque);
        double pesoTotalAtual = calcularPesoTotalAtual();
        double pesoTotalGeral = pesoTotalBanco + pesoTotalAtual;

        if ((pesoTotalGeral + produto.getPeso()) > capacidadeTotal) {
            System.out.println("Erro: Produto excede a capacidade total do estoque.");
            return -1;
        }

        String sql = "INSERT INTO produto (peso, valor, nome, estoque_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDouble(1, produto.getPeso());
            stmt.setDouble(2, produto.getValor());
            stmt.setString(3, produto.getNome());
            stmt.setInt(4, idEstoque);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        produto.setId(generatedKeys.getInt(1)); // Define o ID do banco no objeto Produto
                        produtos.add(produto); // Adiciona o produto à lista em memória

                        // Adiciona uma mensagem de debug para conferir o ID gerado
                        System.out.println("Produto inserido com sucesso no banco com ID: " + produto.getId());
                    } else {
                        System.out.println("Erro: Nenhum ID foi gerado para o produto.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produto.getId();
    }


    protected void atualizarCapacidadeEstoque(int estoque_id, double novaCapacidade) {
        String sql = "UPDATE estoque SET capacidadeTotal = ? WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, novaCapacidade);
            stmt.setInt(2, estoque_id);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Capacidade do estoque atualizada com sucesso.");
            } else {
                System.out.println("Falha ao atualizar a capacidade do estoque.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar capacidade do estoque: " + e.getMessage());
        }
    }

    protected void adicionarEstoqueNoBanco(double capacidadeTotal) {
        String sql = "INSERT INTO estoque (capacidadeTotal) VALUES (?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setDouble(1, this.capacidadeTotal);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    this.estoque_id = rs.getInt(1);
                    System.out.println("Estoque adicionado com sucesso. ID do estoque: " + this.estoque_id);
                }
            } else {
                System.out.println("Falha ao adicionar o estoque.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir estoque: " + e.getMessage());
        }
    }

    protected List<Integer> listarIdsEstoquesNoBanco() {
        List<Integer> idsEstoques = new ArrayList<>();
        String sql = "SELECT id FROM estoque";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                idsEstoques.add(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar IDs dos estoques: " + e.getMessage());
        }

        return idsEstoques;
    }
}
