package br.edu.unirv.esw430.checkout.patterns.adapter;

import br.edu.unirv.esw430.checkout.model.RequisicaoPagamento;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayASdk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AdapterGatewayATest {

    private AdapterGatewayA adapter;

    @BeforeEach
    void setUp() {
        adapter = new AdapterGatewayA(new GatewayASdk(), "MERCHANT-UNIRV-A");
    }

    @Test
    @DisplayName("converte reais em centavos e APPROVED em autorização")
    void aprovaEConverteCentavos() {
        ResultadoAutorizacao resultado = adapter.autorizarTransacao(
                requisicao("10.50", "BRL", "tok_visa_4242"));

        assertThat(resultado.isAutorizado()).isTrue();
        assertThat(resultado.getGateway()).isEqualTo("GATEWAY_A");
        assertThat(resultado.getIdTransacao()).isEqualTo("TXA-PED-X-1050");
        assertThat(resultado.getCodigoAutorizacao()).isEqualTo("A1050");
        assertThat(resultado.getDetalheAdaptacao()).contains("valorCentavos=1050", "status=APPROVED");
    }

    @Test
    @DisplayName("traduz DECLINED sem lançar a exceção do SDK")
    void recusaToken() {
        ResultadoAutorizacao resultado = adapter.autorizarTransacao(
                requisicao("10.50", "BRL", "tok_recusado"));

        assertThat(resultado.isAutorizado()).isFalse();
        assertThat(resultado.getIdTransacao()).isEqualTo("TXA-PED-X-1050");
        assertThat(resultado.getCodigoAutorizacao()).isNull();
        assertThat(resultado.getMensagem()).contains("recusada");
        assertThat(resultado.getDetalheAdaptacao()).contains("status=DECLINED", "DO_NOT_HONOR");
    }

    @Test
    @DisplayName("traduz a exceção de token vazio para resposta negada")
    void tokenVazio() {
        ResultadoAutorizacao resultado = adapter.autorizarTransacao(
                requisicao("10.50", "BRL", " "));

        assertThat(resultado.isAutorizado()).isFalse();
        assertThat(resultado.getMensagem()).contains("cardToken is required");
        assertThat(resultado.getDetalheAdaptacao()).contains("GatewayASdkException");
    }

    @Test
    @DisplayName("não chama o contrato de centavos quando a moeda não é BRL")
    void moedaIncompativel() {
        ResultadoAutorizacao resultado = adapter.autorizarTransacao(
                requisicao("10.00", "USD", "tok_visa_4242"));

        assertThat(resultado.isAutorizado()).isFalse();
        assertThat(resultado.getIdTransacao()).isNull();
        assertThat(resultado.getDetalheAdaptacao()).contains("USD");
    }

    private RequisicaoPagamento requisicao(String valor, String moeda, String token) {
        return new RequisicaoPagamento("PED-X", new BigDecimal(valor), moeda, token, "12345678901", "teste");
    }
}
