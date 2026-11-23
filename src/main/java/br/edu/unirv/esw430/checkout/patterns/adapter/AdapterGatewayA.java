package br.edu.unirv.esw430.checkout.patterns.adapter;

import br.edu.unirv.esw430.checkout.model.RequisicaoPagamento;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayAChargeResponse;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayASdk;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayASdkException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Adapter do Gateway A: converte a requisição do domínio para centavos e o status
 * APPROVED/DECLINED do SDK para {@link ResultadoAutorizacao}.
 */
@Component
public class AdapterGatewayA implements ProcessadorPagamento {

    private final GatewayASdk sdk;
    private final String merchantId;

    public AdapterGatewayA(GatewayASdk sdk,
                           @Value("${checkout.gateway-a.merchant-id:MERCHANT-UNIRV-A}") String merchantId) {
        if (sdk == null) {
            throw new IllegalArgumentException("SDK do Gateway A é obrigatório.");
        }
        this.sdk = sdk;
        this.merchantId = merchantId;
    }

    @Override
    public String getNomeGateway() {
        return GATEWAY_A;
    }

    @Override
    public ResultadoAutorizacao autorizarTransacao(RequisicaoPagamento requisicao) {
        validar(requisicao);
        if (!"BRL".equalsIgnoreCase(requisicao.getMoeda())) {
            return negado("Gateway A aceita apenas moeda BRL.",
                    "Requisição não enviada ao SDK: moeda " + requisicao.getMoeda()
                            + " incompatível com GatewayASdk (valor em centavos, moeda implícita BRL).");
        }

        long centavos;
        try {
            centavos = requisicao.getValor()
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();
        } catch (ArithmeticException ex) {
            return negado("Valor inválido para o Gateway A.",
                    "Falha ao converter BigDecimal " + requisicao.getValor().toPlainString()
                            + " para centavos: " + ex.getMessage());
        }

        String chamada = "GatewayASdk.cobrar(merchantId=%s, valorCentavos=%d, cardToken=%s, orderRef=%s)"
                .formatted(merchantId, centavos, mascarar(requisicao.getTokenCartao()), requisicao.getReferenciaPedido());
        try {
            GatewayAChargeResponse resposta = sdk.cobrar(
                    merchantId,
                    centavos,
                    requisicao.getTokenCartao(),
                    requisicao.getReferenciaPedido());
            boolean autorizado = "APPROVED".equalsIgnoreCase(resposta.getStatus());
            String mensagem = autorizado
                    ? "Transação aprovada no Gateway A."
                    : "Transação recusada no Gateway A.";
            String detalhe = chamada
                    + " -> status=" + resposta.getStatus()
                    + ", transactionId=" + resposta.getTransactionId()
                    + ", authCode=" + resposta.getAuthCode()
                    + ", technicalMessage=" + resposta.getTechnicalMessage();
            return new ResultadoAutorizacao(
                    autorizado,
                    getNomeGateway(),
                    resposta.getTransactionId(),
                    vazioParaNulo(resposta.getAuthCode()),
                    mensagem,
                    detalhe);
        } catch (GatewayASdkException ex) {
            return negado(
                    "Falha técnica traduzida do SDK do Gateway A: " + ex.getMessage(),
                    chamada + " -> exceção " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private ResultadoAutorizacao negado(String mensagem, String detalhe) {
        return new ResultadoAutorizacao(false, getNomeGateway(), null, null, mensagem, detalhe);
    }

    private void validar(RequisicaoPagamento requisicao) {
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

    private String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private String mascarar(String token) {
        if (token == null || token.isBlank()) {
            return "(vazio)";
        }
        if (token.length() <= 4) {
            return "****";
        }
        return "****" + token.substring(token.length() - 4);
    }
}
