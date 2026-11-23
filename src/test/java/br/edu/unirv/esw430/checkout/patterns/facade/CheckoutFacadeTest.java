package br.edu.unirv.esw430.checkout.patterns.facade;

import br.edu.unirv.esw430.checkout.config.FreteProperties;
import br.edu.unirv.esw430.checkout.config.ImpostoProperties;
import br.edu.unirv.esw430.checkout.exception.CheckoutInvalidoException;
import br.edu.unirv.esw430.checkout.exception.PedidoNaoEncontradoException;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCheckout;
import br.edu.unirv.esw430.checkout.model.SolicitacaoCheckout;
import br.edu.unirv.esw430.checkout.model.StatusPedido;
import br.edu.unirv.esw430.checkout.patterns.adapter.AdapterGatewayA;
import br.edu.unirv.esw430.checkout.patterns.adapter.AdapterGatewayB;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayASdk;
import br.edu.unirv.esw430.checkout.patterns.adapter.sdk.GatewayBSdk;
import br.edu.unirv.esw430.checkout.repository.CupomRepositoryMemoria;
import br.edu.unirv.esw430.checkout.repository.PedidoRepositoryMemoria;
import br.edu.unirv.esw430.checkout.service.CalculoPedidoService;
import br.edu.unirv.esw430.checkout.service.MontadorCalculadoraPedidoPadrao;
import br.edu.unirv.esw430.checkout.service.ResolvedorCupom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckoutFacadeTest {

    private PedidoRepositoryMemoria pedidos;
    private CheckoutFacade facade;

    @BeforeEach
    void setUp() {
        pedidos = new PedidoRepositoryMemoria();
        CalculoPedidoService calculo = new CalculoPedidoService(
                new ResolvedorCupom(new CupomRepositoryMemoria()),
                new MontadorCalculadoraPedidoPadrao(FreteProperties.padrao(), ImpostoProperties.padrao()));
        facade = new CheckoutFacade(
                pedidos,
                calculo,
                List.of(
                        new AdapterGatewayA(new GatewayASdk(), "MERCHANT-UNIRV-A"),
                        new AdapterGatewayB(new GatewayBSdk(), "LOJA-UNIRV-B")));
    }

    @Test
    @DisplayName("finaliza PED-1001 no Gateway A e emite comprovante")
    void sucessoGatewayA() {
        ResultadoCheckout resultado = facade.finalizarPedido(
                new SolicitacaoCheckout("PED-1001", "gateway_a", "PROMO10", "tok_visa_4242"));

        assertThat(resultado.isConfirmado()).isTrue();
        assertThat(resultado.getCalculo().getTotal()).isEqualByComparingTo("258.00");
        assertThat(resultado.getAutorizacao().getIdTransacao()).isEqualTo("TXA-PED-1001-25800");
        assertThat(resultado.getComprovante()).isNotNull();
        assertThat(resultado.getComprovante().getValorTotal()).isEqualByComparingTo("258.00");
        assertThat(pedidos.buscarPorId("PED-1001").orElseThrow().getStatus()).isEqualTo(StatusPedido.PAGO);
    }

    @Test
    @DisplayName("finaliza PED-1002 no Gateway B sem cupom")
    void sucessoGatewayB() {
        ResultadoCheckout resultado = facade.finalizarPedido(
                new SolicitacaoCheckout("PED-1002", "GATEWAY_B", null, "tok_master_5555"));

        assertThat(resultado.isConfirmado()).isTrue();
        assertThat(resultado.getCalculo().getTotal()).isEqualByComparingTo("126.99");
        assertThat(resultado.getAutorizacao().getIdTransacao()).isEqualTo("NSU-B-PED-1002");
        assertThat(pedidos.buscarPorId("PED-1002").orElseThrow().getStatus()).isEqualTo(StatusPedido.PAGO);
    }

    @Test
    @DisplayName("recusa o pagamento e grava o pedido como recusado, sem comprovante")
    void pagamentoRecusado() {
        ResultadoCheckout resultado = facade.finalizarPedido(
                new SolicitacaoCheckout("PED-1003", "GATEWAY_A", "VALE20", "tok_recusado"));

        assertThat(resultado.isConfirmado()).isFalse();
        assertThat(resultado.getComprovante()).isNull();
        assertThat(resultado.getCalculo().getTotal()).isEqualByComparingTo("1598.40");
        assertThat(pedidos.buscarPorId("PED-1003").orElseThrow().getStatus()).isEqualTo(StatusPedido.RECUSADO);
    }

    @Test
    @DisplayName("não altera pedido que já não está aberto")
    void pedidoJaPago() {
        assertThatThrownBy(() -> facade.finalizarPedido(
                new SolicitacaoCheckout("PED-1004", "GATEWAY_A", null, "tok_visa_4242")))
                .isInstanceOf(CheckoutInvalidoException.class)
                .hasMessageContaining("não está aberto");
        assertThat(pedidos.buscarPorId("PED-1004").orElseThrow().getStatus()).isEqualTo(StatusPedido.PAGO);
    }

    @Test
    @DisplayName("pedido inexistente não é confundido com recusa de pagamento")
    void pedidoInexistente() {
        assertThatThrownBy(() -> facade.finalizarPedido(
                new SolicitacaoCheckout("PED-9999", "GATEWAY_A", null, "tok_visa_4242")))
                .isInstanceOf(PedidoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("cupom inválido é rejeitado antes de chamar o gateway")
    void cupomInvalidoNaoMudaStatus() {
        assertThatThrownBy(() -> facade.finalizarPedido(
                new SolicitacaoCheckout("PED-1005", "GATEWAY_B", "NAOEXISTE", "tok_visa_4242")))
                .isInstanceOf(CheckoutInvalidoException.class)
                .hasMessageContaining("Cupom inválido");
        assertThat(pedidos.buscarPorId("PED-1005").orElseThrow().getStatus()).isEqualTo(StatusPedido.ABERTO);
    }

    @Test
    @DisplayName("pedido sem itens é rejeitado e a massa inicial pode ser restaurada")
    void pedidoSemItensERestauracao() {
        Pedido original = pedidos.buscarPorId("PED-1005").orElseThrow();
        pedidos.salvar(new Pedido(
                "PED-VAZIO",
                original.getCliente(),
                original.getCepEntrega(),
                StatusPedido.ABERTO,
                List.of()));

        assertThatThrownBy(() -> facade.finalizarPedido(
                new SolicitacaoCheckout("PED-VAZIO", "GATEWAY_A", null, "tok_visa_4242")))
                .isInstanceOf(CheckoutInvalidoException.class)
                .hasMessageContaining("não possui itens");

        facade.finalizarPedido(new SolicitacaoCheckout("PED-1001", "GATEWAY_A", null, "tok_visa_4242"));
        pedidos.recarregarDadosIniciais();
        assertThat(pedidos.buscarPorId("PED-1001").orElseThrow().getStatus()).isEqualTo(StatusPedido.ABERTO);
        assertThat(pedidos.buscarPorId("PED-VAZIO")).isEmpty();
    }

    @Test
    void gatewayDesconhecidoNaoMudaStatus() {
        assertThatThrownBy(() -> facade.finalizarPedido(
                new SolicitacaoCheckout("PED-1005", "GATEWAY_C", null, "tok_visa_4242")))
                .isInstanceOf(CheckoutInvalidoException.class)
                .hasMessageContaining("GATEWAY_A");
        assertThat(pedidos.buscarPorId("PED-1005").orElseThrow().getStatus()).isEqualTo(StatusPedido.ABERTO);
    }
}
