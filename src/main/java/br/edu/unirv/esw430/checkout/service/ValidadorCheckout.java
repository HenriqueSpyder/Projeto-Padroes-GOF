package br.edu.unirv.esw430.checkout.service;

import br.edu.unirv.esw430.checkout.exception.CheckoutInvalidoException;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.SolicitacaoCheckout;
import br.edu.unirv.esw430.checkout.model.StatusPedido;
import org.springframework.stereotype.Component;

/**
 * Regras de validação do fechamento de pedido, separadas da orquestração feita pela fachada.
 */
@Component
public class ValidadorCheckout {

    /** Valida a solicitação e devolve o identificador do pedido já sem espaços. */
    public String exigirPedidoId(SolicitacaoCheckout solicitacao) {
        if (solicitacao == null) {
            throw new CheckoutInvalidoException("Solicitação de checkout é obrigatória.");
        }
        String pedidoId = texto(solicitacao.getPedidoId());
        if (pedidoId.isBlank()) {
            throw new CheckoutInvalidoException("Informe o identificador do pedido.");
        }
        return pedidoId;
    }

    public void exigirPedidoPronto(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new CheckoutInvalidoException(
                    "Pedido " + pedido.getId() + " não está aberto para checkout. Status atual: "
                            + pedido.getStatus().getDescricao() + ".");
        }
        if (pedido.getItens().isEmpty()) {
            throw new CheckoutInvalidoException("Pedido " + pedido.getId() + " não possui itens.");
        }
    }

    /** Valida token e gateway; devolve o token e o gateway normalizados, nessa ordem. */
    public DadosPagamento exigirDadosPagamento(SolicitacaoCheckout solicitacao) {
        String token = texto(solicitacao.getTokenCartao());
        if (token.isBlank()) {
            throw new CheckoutInvalidoException("Informe o token do cartão.");
        }
        String gateway = texto(solicitacao.getGateway());
        if (gateway.isBlank()) {
            throw new CheckoutInvalidoException("Informe o gateway de pagamento.");
        }
        return new DadosPagamento(token, gateway);
    }

    public record DadosPagamento(String token, String gateway) {
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
