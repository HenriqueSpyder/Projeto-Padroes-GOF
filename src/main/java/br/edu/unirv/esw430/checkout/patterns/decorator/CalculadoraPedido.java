package br.edu.unirv.esw430.checkout.patterns.decorator;

import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;

/**
 * Componente do Decorator. Calcula o valor do pedido e pode ser envolvido
 * por frete, imposto e cupom sem alterar o chamador.
 */
public interface CalculadoraPedido {

    ResultadoCalculo calcular(Pedido pedido);
}
