package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Estoque {
    private int estoque_id;
    private double capacidadeTotal;
    private List<Produto> produtos;
    private List<Produto> produtosOtimizados;
    private double capacidadeTotalOriginal;

    public Estoque(double capacidadeTotal) {
        this.capacidadeTotal = capacidadeTotal;
        this.capacidadeTotalOriginal = capacidadeTotal;
        this.produtos = new ArrayList<>();
        this.produtosOtimizados = new ArrayList<>();
    }

    public List<Produto> getProdutosOtimizados() {
        return produtosOtimizados;
    }

    public double getCapacidadeTotal() {
        return capacidadeTotal;
    }

    public double getCapacidadeTotalOriginal() {
        return capacidadeTotalOriginal;
    }

    public void removerProdutoDoBanco(int produtoId) {
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

    public void listarProdutosOtimizados() {
        for (Produto produto : produtosOtimizados) {
            System.out.println("Produto Otimizado: " + produto.getNome() + ", Id: " + produto.getId() + ", Peso: " + produto.getPeso() + " Kg" + ", Valor: " + produto.getValor() + " Reais");
        }
    }

    public void otimizarDistribuicao() {
        int n = produtos.size();
        int capacidadeInt = (int) capacidadeTotal;
        double[][] dp = new double[n + 1][capacidadeInt + 1];
        for (int i = 1; i <= n; i++) {
            Produto produto = produtos.get(i - 1);
            int pesoInt = (int) Math.round(produto.getPeso());
            double valor = produto.getValor();

            if (pesoInt <= 0) {
                System.out.println("Produto com peso inválido: " + produto.getNome());
                continue;
            }

            for (int j = 0; j <= capacidadeInt; j++) {
                if (pesoInt <= j) {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - pesoInt] + valor);
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        int w = capacidadeInt;
        produtosOtimizados.clear();
        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                Produto produto = produtos.get(i - 1);
                produtosOtimizados.add(produto);
                w -= (int) Math.round(produto.getPeso());
            }
        }

        System.out.println("Produtos Otimizados para a Capacidade Total:");
        for (Produto produto : produtosOtimizados) {
            System.out.println(produto.getNome() + " - Peso: " + produto.getPeso() + "kg, Valor: " + produto.getValor());
        }
    }

    public void adicionarProdutoNoBanco(Produto produto, int estoqueId) {
        String sql = "INSERT INTO produto (peso, valor, nome, estoque_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, produto.getPeso());
            stmt.setDouble(2, produto.getValor());
            stmt.setString(3, produto.getNome());
            stmt.setInt(4, estoqueId);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Produto adicionado com sucesso.");
            } else {
                System.out.println("Falha ao adicionar o produto.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir produto: " + e.getMessage());
        }
    }

    public void listarProdutosDoBanco(int estoqueId) {
        String sql = "SELECT * FROM produto WHERE estoque_id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estoqueId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("Produto: " + rs.getString("nome") +
                        ", Peso: " + rs.getDouble("peso") +
                        ", Valor: " + rs.getDouble("valor"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
        }
    }

    public void atualizarCapacidadeEstoque(int estoqueId, double novaCapacidade) {
        String sql = "UPDATE estoque SET capacidadeTotal = ? WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, novaCapacidade);
            stmt.setInt(2, estoqueId);

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

    public void adicionarEstoqueNoBanco() {
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
}

