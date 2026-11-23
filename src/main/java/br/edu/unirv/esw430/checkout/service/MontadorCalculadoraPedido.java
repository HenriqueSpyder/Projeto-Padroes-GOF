package br.edu.unirv.esw430.checkout.service;

import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.patterns.decorator.CalculadoraPedido;

public interface MontadorCalculadoraPedido {

    CalculadoraPedido montar(Cupom cupom);
}
