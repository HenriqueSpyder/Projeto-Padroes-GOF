package br.edu.unirv.esw430.checkout.patterns.adapter;

import br.edu.unirv.esw430.checkout.model.RequisicaoPagamento;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;

/**
 * Interface alvo do Adapter. O checkout depende só deste contrato,
 * sem conhecer o formato dos SDKs externos.
 */
public interface ProcessadorPagamento {

    String GATEWAY_A = "GATEWAY_A";
    String GATEWAY_B = "GATEWAY_B";

    ResultadoAutorizacao autorizarTransacao(RequisicaoPagamento requisicao);

    String getNomeGateway();
}
