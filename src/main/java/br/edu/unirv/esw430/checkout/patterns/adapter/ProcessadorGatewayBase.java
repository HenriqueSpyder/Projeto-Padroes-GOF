package br.edu.unirv.esw430.checkout.patterns.adapter;

import br.edu.unirv.esw430.checkout.model.RequisicaoPagamento;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;

/**
 * Comportamento comum aos adaptadores de gateway: validação da requisição, mascaramento
 * do token e montagem do resultado negado. A tradução específica de cada SDK fica nas subclasses.
 */
abstract class ProcessadorGatewayBase implements ProcessadorPagamento {

    protected void validar(RequisicaoPagamento requisicao) {
        if (requisicao == null) {
            throw new IllegalArgumentException("Requisição de pagamento é obrigatória.");
        }
        if (requisicao.getValor() == null) {
            throw new IllegalArgumentException("Valor da requisição é obrigatório.");
        }
        if (requisicao.getReferenciaPedido() == null || requisicao.getReferenciaPedido().isBlank()) {
            throw new IllegalArgumentException("Referência do pedido é obrigatória.");
        }
    }

    protected ResultadoAutorizacao negado(String mensagem, String detalhe) {
        return new ResultadoAutorizacao(false, getNomeGateway(), null, null, mensagem, detalhe);
    }

    protected String mascarar(String token) {
        if (token == null || token.isBlank()) {
            return "(vazio)";
        }
        if (token.length() <= 4) {
            return "****";
        }
        return "****" + token.substring(token.length() - 4);
    }
}
