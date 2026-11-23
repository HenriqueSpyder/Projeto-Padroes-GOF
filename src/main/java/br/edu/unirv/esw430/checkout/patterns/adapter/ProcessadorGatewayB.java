package br.edu.unirv.esw430.checkout.patterns.adapter;

import br.edu.unirv.esw430.checkout.model.RequisicaoPagamento;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayBRetorno;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayBSdk;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayBSdkException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter do Gateway B: converte BigDecimal em double, envia documento e meio de pagamento,
 * e traduz o código numérico do SDK para autorização do domínio.
 */
@Component
public class ProcessadorGatewayB extends ProcessadorGatewayBase {

    public static final String NOME = "GATEWAY_B";

    private final GatewayBSdk sdk;
    private final String contaLoja;

    public ProcessadorGatewayB(GatewayBSdk sdk,
                           @Value("${checkout.gateway-b.conta-loja:LOJA-UNIRV-B}") String contaLoja) {
        if (sdk == null) {
            throw new IllegalArgumentException("SDK do Gateway B é obrigatório.");
        }
        this.sdk = sdk;
        this.contaLoja = contaLoja;
    }

    @Override
    public String getNomeGateway() {
        return NOME;
    }

    @Override
    public ResultadoAutorizacao autorizarTransacao(RequisicaoPagamento requisicao) {
        validar(requisicao);
        if (!"BRL".equalsIgnoreCase(requisicao.getMoeda())) {
            return negado("Gateway B aceita apenas moeda BRL.",
                    "Requisição não enviada ao SDK: moeda " + requisicao.getMoeda()
                            + " incompatível com GatewayBSdk.pagar (double em reais e código de retorno inteiro).");
        }

        double valorReais = requisicao.getValor().doubleValue();
        String documento = requisicao.getDocumentoCliente() == null ? "" : requisicao.getDocumentoCliente();
        String chamada = "GatewayBSdk.pagar(contaLoja=%s, valorReais=%s [BigDecimal %s], documento=%s, referencia=%s, meioPagamento=%s)"
                .formatted(
                        contaLoja,
                        Double.toString(valorReais),
                        requisicao.getValor().toPlainString(),
                        documento,
                        requisicao.getReferenciaPedido(),
                        mascarar(requisicao.getTokenCartao()));
        try {
            GatewayBRetorno retorno = sdk.pagar(
                    contaLoja,
                    valorReais,
                    documento,
                    requisicao.getReferenciaPedido(),
                    requisicao.getTokenCartao());
            boolean autorizado = retorno.getCodigoRetorno() == 0;
            String detalhe = chamada
                    + " -> codigoRetorno=" + retorno.getCodigoRetorno()
                    + ", nsu=" + retorno.getNsu()
                    + ", mensagemOperadora=" + retorno.getMensagemOperadora();
            return new ResultadoAutorizacao(
                    autorizado,
                    getNomeGateway(),
                    retorno.getNsu(),
                    autorizado ? String.valueOf(retorno.getCodigoRetorno()) : null,
                    traduzirMensagem(retorno),
                    detalhe);
        } catch (GatewayBSdkException ex) {
            return negado(
                    "Falha técnica traduzida do SDK do Gateway B: " + ex.getMessage(),
                    chamada + " -> exceção " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private String traduzirMensagem(GatewayBRetorno retorno) {
        if (retorno.getCodigoRetorno() == 0) {
            return "Transação aprovada no Gateway B.";
        }
        if ("LIMITE_EXCEDIDO".equals(retorno.getMensagemOperadora())) {
            return "Transação recusada no Gateway B: limite da operadora excedido.";
        }
        if (retorno.getCodigoRetorno() == 51) {
            return "Transação recusada no Gateway B.";
        }
        return "Transação não concluída no Gateway B (código " + retorno.getCodigoRetorno() + ").";
    }
}
