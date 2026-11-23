package br.edu.unirv.esw430.checkout.model;

public class Cliente {

    private final String nome;
    private final String email;
    private final String documento;

    public Cliente(String nome, String email, String documento) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório.");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("Documento do cliente é obrigatório.");
        }
        this.nome = nome.trim();
        this.email = email == null ? "" : email.trim();
        this.documento = documento.trim();
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getDocumento() {
        return documento;
    }
}
