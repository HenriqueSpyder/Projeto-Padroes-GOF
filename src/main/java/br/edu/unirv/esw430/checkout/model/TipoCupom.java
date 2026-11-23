package br.edu.unirv.esw430.checkout.model;

public enum TipoCupom {
    PERCENTUAL("Percentual"),
    VALOR_FIXO("Valor fixo");

    private final String descricao;

    TipoCupom(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
