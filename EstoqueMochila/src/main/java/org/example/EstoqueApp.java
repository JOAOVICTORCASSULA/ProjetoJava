package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class EstoqueApp {
    private Estoque estoque;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            double capacidade = solicitarCapacidadeInicial();
            if (capacidade > 0) {
                new EstoqueApp(capacidade).createAndShowGUI();
            } else {
                JOptionPane.showMessageDialog(null, "Capacidade inválida. Encerrando aplicação.");
            }
        });
    }

    public EstoqueApp(double capacidadeTotal) {
        this.estoque = new Estoque(capacidadeTotal);
        try {
            estoque.adicionarEstoqueNoBanco(capacidadeTotal);
            JOptionPane.showMessageDialog(null, "Estoque inicial adicionado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao adicionar estoque inicial: " + e.getMessage());
        }
    }


    private static double solicitarCapacidadeInicial() {
        String capacidadeStr = JOptionPane.showInputDialog(null, "Digite a capacidade total do estoque:");
        try {
            return Double.parseDouble(capacidadeStr);
        } catch (NumberFormatException e) {
            return -1; // Retorna valor inválido para encerrar
        }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Gerenciador de Estoque");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        String imagePath = "src/main/java/org/example/imagem.jpg.jpg"; // Caminho da imagem

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Menu", createMenuTab(imagePath));
        tabbedPane.addTab("Otimização", createOtimizacaoTab(imagePath));

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createMenuTab(String imagePath) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 10));

        buttonPanel.add(createButtonWithIcon("Listar Id dos Estoques", null, e -> listarIdsEstoques()));
        buttonPanel.add(createButtonWithIcon("Listar Produtos", null, e -> listarProdutos()));
        buttonPanel.add(createButtonWithIcon("Adicionar Estoque", null, e -> adicionarEstoque()));
        buttonPanel.add(createButtonWithIcon("Adicionar Produtos", null, e -> adicionarProduto()));
        buttonPanel.add(createButtonWithIcon("Remover Produtos", null, e -> removerProduto()));
        buttonPanel.add(createButtonWithIcon("Alterar Estoque", null, e -> alterarEstoque()));

        JLabel imageLabel = new JLabel();
        loadImage(imageLabel, imagePath);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonPanel, imageLabel);
        splitPane.setDividerLocation(400);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createOtimizacaoTab(String imagePath) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // Botões para otimizar e listar produtos otimizados
        JButton botao1 = createButtonWithIcon("Fazer Otimização", null, e -> otimizarDistribuicao());
        buttonPanel.add(botao1);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        for (Component comp : buttonPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setPreferredSize(new Dimension(400, 80));
                button.setMaximumSize(new Dimension(400, 80));
                button.setMinimumSize(new Dimension(400, 80));
            }
        }

        JLabel imageLabel = new JLabel();
        loadImage(imageLabel, imagePath);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonPanel, imageLabel);
        splitPane.setDividerLocation(400);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private void loadImage(JLabel imageLabel, String imagePath) {
        try {
            ImageIcon icon = new ImageIcon(imagePath);
            Image image = icon.getImage();
            int width = 400;
            int height = (int) (width * image.getHeight(null) / (double) image.getWidth(null) * 1.35);
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar imagem: " + e.getMessage());
            imageLabel.setText("Imagem não encontrada!");
        }
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private JButton createButtonWithIcon(String text, String iconPath, ActionListener action) {
        JButton button = new JButton(text);

        if (iconPath != null) {
            ImageIcon icon = new ImageIcon(iconPath);
            button.setIcon(icon);
        }

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(action);
        return button;
    }

    private void otimizarDistribuicao() {
        String idEstoqueStr = JOptionPane.showInputDialog(null, "Digite o ID do estoque:");
        if (idEstoqueStr == null || idEstoqueStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "ID do estoque não pode ser vazio.");
            return;
        }

        try {
            int idEstoque = Integer.parseInt(idEstoqueStr);

            // Aqui, a instância de Estoque é usada para chamar o método
            Estoque estoque = this.estoque.buscarEstoquePorId(idEstoque); // Aqui você já está chamando diretamente

            if (estoque == null) {
                JOptionPane.showMessageDialog(null, "Estoque não encontrado.");
                return;
            }

            // Solicitando a capacidade da mochila ao usuário
            String capacidadeStr = JOptionPane.showInputDialog(null, "Digite a capacidade da mochila:");
            if (capacidadeStr == null || capacidadeStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Capacidade da mochila não pode ser vazia.");
                return;
            }

            double capacidadeMochila = Double.parseDouble(capacidadeStr);
            if (capacidadeMochila <= 0) {
                JOptionPane.showMessageDialog(null, "A capacidade deve ser um número positivo.");
                return;
            }

            // Obtenção dos produtos e otimização da distribuição
            List<Produto> produtos = estoque.getProdutos();
            int n = produtos.size();

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

            // Exibindo os resultados na interface gráfica
            if (produtosEscolhidos.length() > 0) {
                JOptionPane.showMessageDialog(null, "Produtos escolhidos para carregar na mochila:\n" + produtosEscolhidos.toString());
            } else {
                JOptionPane.showMessageDialog(null, "Nenhum produto foi selecionado.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido ou capacidade inválida. Por favor, insira um número.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro durante a otimização: " + e.getMessage());
        }
    }
    
    private void listarIdsEstoques() {
        try {
            List<Integer> idsEstoques = estoque.listarIdsEstoquesNoBanco();
            if (idsEstoques.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Não há estoques cadastrados.");
            } else {
                mostrarDialogoListagemIds("IDs dos Estoques", idsEstoques);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar os IDs dos estoques: " + e.getMessage());
        }
    }

    private void listarProdutos() {
        String idEstoque = JOptionPane.showInputDialog(null, "Digite o ID do estoque:");
        try {
            List<Produto> produtos = estoque.listarProdutosDoBanco(Integer.parseInt(idEstoque));
            mostrarDialogoListagem("Produtos no Estoque", produtos);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido. Tente novamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar produtos: " + e.getMessage());
        }
    }

    private void adicionarEstoque() {
        String capacidadeStr = JOptionPane.showInputDialog(null, "Digite a capacidade total do estoque:");
        try {
            double capacidade = Double.parseDouble(capacidadeStr);
            estoque.adicionarEstoqueNoBanco(capacidade);
            JOptionPane.showMessageDialog(null, "Estoque adicionado com sucesso!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Capacidade inválida. Tente novamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao adicionar estoque: " + e.getMessage());
        }
    } 

    private void adicionarProduto() {
        String idEstoqueStr = JOptionPane.showInputDialog(null, "Digite o ID do estoque:");
        if (idEstoqueStr == null || idEstoqueStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "ID do estoque não pode ser vazio.");
            return;
        }
        String nomeProduto = JOptionPane.showInputDialog(null, "Digite o nome do produto:");
        if (nomeProduto == null || nomeProduto.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nome do produto não pode ser vazio.");
            return;
        }
        String pesoStr = JOptionPane.showInputDialog(null, "Digite o peso do produto:");
        String valorStr = JOptionPane.showInputDialog(null, "Digite o valor do produto:");
        if (pesoStr == null || valorStr == null || pesoStr.isEmpty() || valorStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Peso ou valor do produto não podem ser vazios.");
            return;
        }
        try {
            double peso = Double.parseDouble(pesoStr);
            double valor = Double.parseDouble(valorStr);
            Produto produto = new Produto(peso, valor, nomeProduto);
            int idProduto = estoque.adicionarProdutoNoBanco(produto, Integer.parseInt(idEstoqueStr));
            if (idProduto != -1) {
                JOptionPane.showMessageDialog(null, "Produto adicionado com sucesso! ID do Produto: " + idProduto);
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao adicionar produto.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Valor de peso ou valor inválidos. Tente novamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao adicionar produto: " + e.getMessage());
        }
    }

    private void removerProduto() {
        String idProdutoStr = JOptionPane.showInputDialog(null, "Digite o ID do produto:");
        if (idProdutoStr == null || idProdutoStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "ID do produto não pode ser vazio.");
            return;
        }
        try {
            int idProduto = Integer.parseInt(idProdutoStr);
            estoque.removerProdutoDoBanco(idProduto);
            JOptionPane.showMessageDialog(null, "Produto removido com sucesso.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao remover produto: " + e.getMessage());
        }
    }

    private void alterarEstoque() {
        String idEstoque = JOptionPane.showInputDialog(null, "Digite o ID do estoque:");
        String capacidadeStr = JOptionPane.showInputDialog(null, "Digite a nova capacidade do estoque:");
        try {
            double novaCapacidade = Double.parseDouble(capacidadeStr);
            estoque.atualizarCapacidadeEstoque(Integer.parseInt(idEstoque), novaCapacidade);
            JOptionPane.showMessageDialog(null, "Estoque atualizado com sucesso!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Valor inválido. Tente novamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar estoque: " + e.getMessage());
        }
    }

    // Métodos auxiliares para mostrar listagens

    private void mostrarDialogoListagem(String titulo, List<Produto> produtos) {
        StringBuilder sb = new StringBuilder();
        for (Produto produto : produtos) {
            sb.append(produto.toString()).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString(), titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarDialogoListagemIds(String titulo, List<Integer> idsEstoques) {
        StringBuilder sb = new StringBuilder();
        for (Integer id : idsEstoques) {
            sb.append(id).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString(), titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}
