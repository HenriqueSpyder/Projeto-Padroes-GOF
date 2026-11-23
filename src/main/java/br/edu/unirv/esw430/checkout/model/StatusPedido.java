package br.edu.unirv.esw430.checkout.model;

public enum StatusPedido {
    ABERTO("Aberto"),
    PAGO("Pago"),
    RECUSADO("Recusado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
