package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Estoque {
    private int estoque_id;
    private static int contadorIds = 1;
    private final double capacidadeTotal;
    private final List<Produto> produtos;
    private final List<Produto> produtosOtimizados;
    private final double capacidadeTotalOriginal;

    public Estoque(double capacidadeTotal) {
        this.estoque_id = contadorIds++;
        this.capacidadeTotal = capacidadeTotal;
        this.capacidadeTotalOriginal = capacidadeTotal;
        this.produtos = new ArrayList<>();
        this.produtosOtimizados = new ArrayList<>();
    }

    public int getEstoque_id() {
        return estoque_id;
    }

    public double getCapacidadeTotal() {
        return capacidadeTotal;
    }

    public double getCapacidadeTotalOriginal() {
        return capacidadeTotalOriginal;
    }

    public List<Produto> getProdutosOtimizados() {
        return produtosOtimizados;
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

    protected List<Produto> listarProdutosOtimizados(int estoque_id) {
        for (Produto produto : produtosOtimizados) {
            System.out.println("Produto Otimizado: " + produto.getNome() + ", Id: " + produto.getId() + ", Peso: " + produto.getPeso() + " Kg" + ", Valor: " + produto.getValor() + " Reais");
        }
        return null;
    }

    protected void otimizarDistribuicao(int estoque_id) {
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

    public int adicionarProdutoNoBanco(Produto produto, int idEstoque) {
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
                        produto.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produto.getId(); // Agora o ID correto será retornado
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
                produtos.add(produto);
                System.out.println("Produto: " + produto.getNome() +
                        ", Peso: " + produto.getPeso() +
                        ", Valor: " + produto.getValor());
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
        }
        return produtos;
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

    protected void adicionarEstoqueNoBanco() {
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

    protected boolean verificarEstoquePorId(int estoqueId) {
        String sql = "SELECT id FROM estoque WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estoqueId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Estoque encontrado com o ID: " + estoqueId);
                return true;
            } else {
                System.out.println("Estoque não encontrado com o ID: " + estoqueId);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar estoque: " + e.getMessage());
            return false;
        }
    }
}
