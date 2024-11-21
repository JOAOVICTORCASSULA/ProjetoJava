package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class EstoqueApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EstoqueApp().createAndShowGUI());
    }

    private Estoque estoque;

    public EstoqueApp() {
        this.estoque = new Estoque(1);
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Gerenciador de Estoque");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        String imagePath = "src/main/java/org/example/imagem.jpg.jpg";  // Caminho da imagem

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

        JButton botao1 = createButtonWithIcon("Fazer Otimização", null, e -> otimizarDistribuicao());
        buttonPanel.add(botao1);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton botao2 = createButtonWithIcon("Listar Produtos Otimizados", null, e -> listarProdutosOtimizados());
        buttonPanel.add(botao2);

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
            e.printStackTrace();
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

    // Ações para os botões

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
        }
    }

    private void adicionarEstoque() {
        String capacidadeStr = JOptionPane.showInputDialog(null, "Digite a capacidade total do estoque:");
        try {
            double capacidade = Double.parseDouble(capacidadeStr);
            estoque.adicionarEstoqueNoBanco();
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

            // Aqui é onde o banco de dados entra em ação
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
        String idEstoque = JOptionPane.showInputDialog(null, "Digite o ID do estoque:");
        String idProdutoStr = JOptionPane.showInputDialog(null, "Digite o ID do produto a ser removido:");
        try {
            int idProduto = Integer.parseInt(idProdutoStr);
            estoque.removerProdutoDoBanco(idProduto);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido. Tente novamente.");
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
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Capacidade inválida. Tente novamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar estoque: " + e.getMessage());
        }
    }

    private void otimizarDistribuicao() {
        String idEstoque = JOptionPane.showInputDialog(null, "Digite o ID do estoque:");
        try {
            estoque.otimizarDistribuicao(Integer.parseInt(idEstoque));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido. Tente novamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao otimizar distribuição: " + e.getMessage());
        }
    }

    private void listarProdutosOtimizados() {
        String idEstoque = JOptionPane.showInputDialog(null, "Digite o ID do estoque:");
        try {
            List<Produto> produtosOtimizados = estoque.listarProdutosOtimizados(Integer.parseInt(idEstoque));
            if (produtosOtimizados.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Nenhum produto otimizado encontrado.");
            } else {
                mostrarDialogoListagem("Produtos Otimizados", produtosOtimizados);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido. Tente novamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar produtos otimizados: " + e.getMessage());
        }
    }

    private void mostrarDialogoListagem(String titulo, List<Produto> produtos) {
        JDialog dialog = new JDialog();
        dialog.setTitle(titulo);
        dialog.setSize(400, 300);

        JTextArea textArea = new JTextArea();
        // Exibe a lista de produtos com IDs
        for (Produto produto : produtos) {
            // Verifique se o ID é válido
            textArea.append("ID: " + + produto.getId() +
                    " | Nome: " + produto.getNome() + " | Peso: " + produto.getPeso() + " | Valor: " + produto.getValor() + "\n");
        }

        textArea.setEditable(false);
        dialog.add(new JScrollPane(textArea));
        dialog.setVisible(true);
    }


    private void mostrarDialogoListagemIds(String titulo, List<Integer> ids) {
        JDialog dialog = new JDialog();
        dialog.setTitle(titulo);
        dialog.setSize(400, 300);

        JTextArea textArea = new JTextArea();
        for (Integer id : ids) {
            textArea.append("ID do Estoque: " + id + "\n");
        }

        textArea.setEditable(false);
        dialog.add(new JScrollPane(textArea));
        dialog.setVisible(true);
    }
}


