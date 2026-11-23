package br.edu.unirv.esw430.checkout.controller.dto;

import br.edu.unirv.esw430.checkout.model.SolicitacaoCheckout;
import jakarta.validation.constraints.NotBlank;

public class SolicitacaoCheckoutForm {

    @NotBlank(message = "Informe o pedido.")
    private String pedidoId;

    @NotBlank(message = "Informe o gateway.")
    private String gateway;

    private String codigoCupom;

    @NotBlank(message = "Informe o token do cartão.")
    private String tokenCartao;

    public SolicitacaoCheckout paraSolicitacao() {
        return new SolicitacaoCheckout(pedidoId, gateway, codigoCupom, tokenCartao);
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCodigoCupom() {
        return codigoCupom;
    }

    public void setCodigoCupom(String codigoCupom) {
        this.codigoCupom = codigoCupom;
    }

    public String getTokenCartao() {
        return tokenCartao;
    }

    public void setTokenCartao(String tokenCartao) {
        this.tokenCartao = tokenCartao;
    }
}
