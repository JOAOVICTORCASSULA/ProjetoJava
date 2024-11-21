package org.example;

import java.sql.Connection;
import java.sql.SQLException;

public class TesteEstoque {
   public static void main(String[] args) {
        Estoque estoque = new Estoque(1000);
        estoque.adicionarEstoqueNoBanco();
        estoque.removerProdutoDoBanco(9);
        estoque.atualizarCapacidadeEstoque(1, 1000);
        Produto produto1 = new Produto(10.5, 50.0, "Produto 1");
        Produto produto2 = new Produto(20.0, 150.0, "Produto 2");
        estoque.adicionarProdutoNoBanco(produto1, 1);
        estoque.adicionarProdutoNoBanco(produto2, 1);
        estoque.listarProdutosDoBanco(1);
        System.out.println("ID do produto inserido: " + produto2.getId());

   }
}