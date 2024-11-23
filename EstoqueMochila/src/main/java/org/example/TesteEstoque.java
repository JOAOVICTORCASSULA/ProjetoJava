package org.example;

import java.util.List;
import java.util.Random;

public class TesteEstoque {
     public static void main(String[] args) {
          Estoque estoque = new Estoque(100);
          estoque.adicionarEstoqueNoBanco(100);
          Produto produto1 = new Produto(1, 2, "Produto 1");
          Produto produto2 = new Produto(9, 4, "Produto 2");
          Produto produto3 = new Produto(3, 6, "Produto 3");
          Produto produto4 = new Produto(2, 9, "Produto 4");
          Produto produto5 = new Produto(8, 12, "Produto 5");
          Produto produto6 = new Produto(2, 1, "Produto 6");
          Produto produto7 = new Produto(3, 4, "Produto 7");
          Produto produto8 = new Produto(6, 3, "Produto 8");
          Produto produto9 = new Produto(4, 7, "Produto 9");
          Produto produto10 = new Produto(3, 8, "Produto 10");
          estoque.adicionarProdutoNoBanco(produto1, 1);
          estoque.adicionarProdutoNoBanco(produto2, 1);
          estoque.adicionarProdutoNoBanco(produto3, 1);
          estoque.adicionarProdutoNoBanco(produto4, 1);
          estoque.adicionarProdutoNoBanco(produto5, 1);
          estoque.adicionarProdutoNoBanco(produto6, 1);
          estoque.adicionarProdutoNoBanco(produto7, 1);
          estoque.adicionarProdutoNoBanco(produto8, 1);
          estoque.adicionarProdutoNoBanco(produto9, 1);
          estoque.adicionarProdutoNoBanco(produto10, 1);
          System.out.println("\nProdutos no estoque:");
          List<Produto> produtos = estoque.listarProdutosDoBanco(estoque.estoque_id);
          for (Produto p : produtos) {
               System.out.println(p.getNome() + " - Peso: " + p.getPeso() + " - Valor: " + p.getValor());
          }
          estoque.otimizarDistribuicao(10);
     }
}
