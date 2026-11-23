package br.edu.unirv.esw430.checkout.patterns.decorator;

import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;

import java.math.BigDecimal;

/**
 * Componente concreto: soma preço unitário vezes quantidade de cada item.
 */
public class CalculoPedidoBase implements CalculadoraPedido {

    @Override
    public ResultadoCalculo calcular(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido é obrigatório para o cálculo.");
        }
        BigDecimal subtotal = pedido.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ResultadoCalculo.doSubtotal(subtotal);
    }
}
