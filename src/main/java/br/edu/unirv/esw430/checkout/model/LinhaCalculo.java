package br.edu.unirv.esw430.checkout.model;

import java.math.BigDecimal;

public class LinhaCalculo {

    private final String descricao;
    private final BigDecimal valor;

    public LinhaCalculo(String descricao, BigDecimal valor) {
        this.descricao = descricao;
        this.valor = Dinheiro.arredondar(valor);
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
