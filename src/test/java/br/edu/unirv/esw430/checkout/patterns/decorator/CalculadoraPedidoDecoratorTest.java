package br.edu.unirv.esw430.checkout.patterns.decorator;

import br.edu.unirv.esw430.checkout.config.FreteProperties;
import br.edu.unirv.esw430.checkout.config.ImpostoProperties;
import br.edu.unirv.esw430.checkout.model.Cliente;
import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.model.ItemPedido;
import br.edu.unirv.esw430.checkout.model.LinhaCalculo;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;
import br.edu.unirv.esw430.checkout.model.StatusPedido;
import br.edu.unirv.esw430.checkout.model.TipoCupom;
import br.edu.unirv.esw430.checkout.repository.CupomRepositoryMemoria;
import br.edu.unirv.esw430.checkout.repository.PedidoRepositoryMemoria;
import br.edu.unirv.esw430.checkout.service.CalculoPedidoService;
import br.edu.unirv.esw430.checkout.service.MontadorCalculadoraPedidoPadrao;
import br.edu.unirv.esw430.checkout.service.ResolvedorCupom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraPedidoDecoratorTest {

    private PedidoRepositoryMemoria pedidos;
    private CalculoPedidoService calculo;

    @BeforeEach
    void setUp() {
        pedidos = new PedidoRepositoryMemoria();
        calculo = new CalculoPedidoService(
                new ResolvedorCupom(new CupomRepositoryMemoria()),
                new MontadorCalculadoraPedidoPadrao(FreteProperties.padrao(), ImpostoProperties.padrao()));
    }

    @Test
    @DisplayName("PROMO10, frete regional e imposto de 8% fecham em 258,00")
    void pedido1001ComPromo10() {
        ResultadoCalculo resultado = calculo.calcular(pedidos.buscarPorId("PED-1001").orElseThrow(), "PROMO10");

        assertThat(calculo.descreverCadeia("PROMO10"))
                .isEqualTo("CalculoPedidoBase -> CupomDescontoDecorator -> FreteDecorator -> ImpostoDecorator");
        assertThat(resultado.getSubtotal()).isEqualByComparingTo("250.00");
        assertThat(resultado.getDesconto()).isEqualByComparingTo("25.00");
        assertThat(resultado.getFrete()).isEqualByComparingTo("15.00");
        assertThat(resultado.getImposto()).isEqualByComparingTo("18.00");
        assertThat(resultado.getTotal()).isEqualByComparingTo("258.00");
        assertThat(soma(resultado)).isEqualByComparingTo("258.00");
    }

    @Test
    @DisplayName("sem cupom o frete nacional e o imposto entram na cadeia menor")
    void pedido1002SemCupom() {
        ResultadoCalculo resultado = calculo.calcular(pedidos.buscarPorId("PED-1002").orElseThrow(), " ");

        assertThat(calculo.descreverCadeia(null))
                .isEqualTo("CalculoPedidoBase -> FreteDecorator -> ImpostoDecorator");
        assertThat(resultado.getDesconto()).isEqualByComparingTo("0.00");
        assertThat(resultado.getFrete()).isEqualByComparingTo("29.90");
        assertThat(resultado.getImposto()).isEqualByComparingTo("7.19");
        assertThat(resultado.getTotal()).isEqualByComparingTo("126.99");
    }

    @Test
    @DisplayName("subtotal a partir de 300 zera o frete e o vale é abatido antes do imposto")
    void pedido1003Vale20() {
        ResultadoCalculo resultado = calculo.calcular(pedidos.buscarPorId("PED-1003").orElseThrow(), "vale20");

        assertThat(resultado.getDesconto()).isEqualByComparingTo("20.00");
        assertThat(resultado.getFrete()).isEqualByComparingTo("0.00");
        assertThat(resultado.getImposto()).isEqualByComparingTo("118.40");
        assertThat(resultado.getTotal()).isEqualByComparingTo("1598.40");
    }

    @Test
    @DisplayName("cupom de valor fixo não deixa o desconto passar do subtotal")
    void descontoLimitadoAoSubtotal() {
        Pedido pedido = new Pedido(
                "PED-T",
                new Cliente("Teste", "teste@email.com", "12345678901"),
                "01310-100",
                StatusPedido.ABERTO,
                List.of(new ItemPedido("X", "Item barato", 1, new BigDecimal("10.00"))));
        CalculadoraPedido calculadora = new ImpostoDecorator(
                new FreteDecorator(
                        new CupomDescontoDecorator(
                                new CalculoPedidoBase(),
                                new Cupom("VALE20", TipoCupom.VALOR_FIXO, new BigDecimal("20.00"), "vale")),
                        new BigDecimal("15.00"),
                        new BigDecimal("29.90"),
                        new BigDecimal("300.00"),
                        List.of("74", "75")),
                new BigDecimal("0.08"));

        ResultadoCalculo resultado = calculadora.calcular(pedido);

        assertThat(resultado.getDesconto()).isEqualByComparingTo("10.00");
        assertThat(resultado.getFrete()).isEqualByComparingTo("29.90");
        assertThat(resultado.getImposto()).isEqualByComparingTo("0.00");
        assertThat(resultado.getTotal()).isEqualByComparingTo("29.90");
        assertThat(resultado.getLinhas()).extracting(LinhaCalculo::getDescricao)
                .anyMatch(descricao -> descricao.contains("limitado ao subtotal"));
    }

    @Test
    @DisplayName("o componente base não aceita pedido nulo")
    void pedidoNulo() {
        assertThatThrownBy(() -> new CalculoPedidoBase().calcular(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private BigDecimal soma(ResultadoCalculo resultado) {
        return resultado.getLinhas().stream()
                .map(LinhaCalculo::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
