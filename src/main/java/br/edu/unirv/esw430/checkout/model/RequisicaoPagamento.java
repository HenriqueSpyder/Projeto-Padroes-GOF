package br.edu.unirv.esw430.checkout.model;

import java.math.BigDecimal;

public class RequisicaoPagamento {

    private final String referenciaPedido;
    private final BigDecimal valor;
    private final String moeda;
    private final String tokenCartao;
    private final String documentoCliente;
    private final String descricao;

    public RequisicaoPagamento(String referenciaPedido, BigDecimal valor, String moeda,
                               String tokenCartao, String documentoCliente, String descricao) {
        this.referenciaPedido = referenciaPedido;
        this.valor = valor;
        this.moeda = moeda;
        this.tokenCartao = tokenCartao;
        this.documentoCliente = documentoCliente;
        this.descricao = descricao;
    }

    public String getReferenciaPedido() {
        return referenciaPedido;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getMoeda() {
        return moeda;
    }

    public String getTokenCartao() {
        return tokenCartao;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public String getDescricao() {
        return descricao;
    }
}
