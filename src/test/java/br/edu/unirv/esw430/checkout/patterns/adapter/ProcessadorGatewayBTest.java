package br.edu.unirv.esw430.checkout.patterns.adapter;

import br.edu.unirv.esw430.checkout.model.RequisicaoPagamento;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayBSdk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessadorGatewayBTest {

    private ProcessadorGatewayB adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProcessadorGatewayB(new GatewayBSdk(), "LOJA-UNIRV-B");
    }

    @Test
    @DisplayName("traduz codigoRetorno 0 e envia o valor como double")
    void aprova() {
        ResultadoAutorizacao resultado = adapter.autorizarTransacao(
                requisicao("89.90", "BRL", "tok_master_5555"));

        assertThat(resultado.isAutorizado()).isTrue();
        assertThat(resultado.getGateway()).isEqualTo("GATEWAY_B");
        assertThat(resultado.getIdTransacao()).isEqualTo("NSU-B-PED-Y");
        assertThat(resultado.getCodigoAutorizacao()).isEqualTo("0");
        assertThat(resultado.getDetalheAdaptacao()).contains("BigDecimal 89.90", "codigoRetorno=0", "98765432100");
    }

    @Test
    @DisplayName("traduz codigoRetorno 51 quando o valor passa de 5000")
    void limiteExcedido() {
        ResultadoAutorizacao resultado = adapter.autorizarTransacao(
                requisicao("5000.01", "BRL", "tok_ok"));

        assertThat(resultado.isAutorizado()).isFalse();
        assertThat(resultado.getMensagem()).contains("limite");
        assertThat(resultado.getDetalheAdaptacao()).contains("codigoRetorno=51", "LIMITE_EXCEDIDO");
    }

    @Test
    @DisplayName("recusa token incompatível com o meio de pagamento do SDK")
    void tokenRecusado() {
        ResultadoAutorizacao resultado = adapter.autorizarTransacao(
                requisicao("40.00", "BRL", "tok_recusado"));

        assertThat(resultado.isAutorizado()).isFalse();
        assertThat(resultado.getDetalheAdaptacao()).contains("NAO_AUTORIZADO");
    }

    private RequisicaoPagamento requisicao(String valor, String moeda, String token) {
        return new RequisicaoPagamento("PED-Y", new BigDecimal(valor), moeda, token, "98765432100", "teste");
    }
}
