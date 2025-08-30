package br.edu.infnet.dr3tp2.model;

/**
 * Representa um paciente a ser reembolsado
 */
public class Paciente {

    private String nome;
    private String cpf;

    public Paciente() {}

    public Paciente(String nome, String cpf) {
        this.nome = nome;
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
