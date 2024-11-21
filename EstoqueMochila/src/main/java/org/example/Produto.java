package org.example;

public class Produto {
    private double peso;
    private double valor;
    private String nome;
    private int id;

    public Produto(double peso, double valor, String nome) {
        setPeso(peso);
        setValor(valor);
        setNome(nome);
    }

    void setId(int id) {
        if (id <= 0) {
            System.out.println("Erro: O id está vazio.");
            System.exit(1);
        }
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getPeso() {
        return peso;
    }

    public double getValor() {
        return valor;
    }

    public String getNome() {
        return nome;
    }

    private void setPeso(double peso) {
        if (peso <= 0) {
            System.out.println("Erro: Peso não permitido.");
            System.exit(1);
        }
        this.peso = peso;
    }

    private void setValor(double valor) {
        if (valor <= 0) {
            System.out.println("Erro: Valor não permitido.");
            System.exit(1);
        }
        this.valor = valor;
    }

    private void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            System.out.println("Erro: O nome está vazio, é nulo, ou contém apenas espaços.");
            System.exit(1);
        }
        this.nome = nome;
    }


}
