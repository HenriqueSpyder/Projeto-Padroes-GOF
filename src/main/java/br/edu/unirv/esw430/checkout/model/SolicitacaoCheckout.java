package br.edu.unirv.esw430.checkout.model;

public class SolicitacaoCheckout {

    private final String pedidoId;
    private final String gateway;
    private final String codigoCupom;
    private final String tokenCartao;

    public SolicitacaoCheckout(String pedidoId, String gateway, String codigoCupom, String tokenCartao) {
        this.pedidoId = pedidoId;
        this.gateway = gateway;
        this.codigoCupom = codigoCupom;
        this.tokenCartao = tokenCartao;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public String getGateway() {
        return gateway;
    }

    public String getCodigoCupom() {
        return codigoCupom;
    }

    public String getTokenCartao() {
        return tokenCartao;
    }
}
