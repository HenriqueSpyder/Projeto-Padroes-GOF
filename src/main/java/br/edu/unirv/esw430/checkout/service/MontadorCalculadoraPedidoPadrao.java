package br.edu.unirv.esw430.checkout.service;

import br.edu.unirv.esw430.checkout.config.FreteProperties;
import br.edu.unirv.esw430.checkout.config.ImpostoProperties;
import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.patterns.decorator.CalculoPedidoBase;
import br.edu.unirv.esw430.checkout.patterns.decorator.CalculadoraPedido;
import br.edu.unirv.esw430.checkout.patterns.decorator.CupomDescontoDecorator;
import br.edu.unirv.esw430.checkout.patterns.decorator.FreteDecorator;
import br.edu.unirv.esw430.checkout.patterns.decorator.ImpostoDecorator;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Monta a cadeia de decorators na ordem em que as regras dependem umas das outras:
 * subtotal, depois cupom, depois frete e, por fora, o imposto.
 */
@Service
public class MontadorCalculadoraPedidoPadrao implements MontadorCalculadoraPedido {

    private final FreteProperties freteProperties;
    private final ImpostoProperties impostoProperties;

    public MontadorCalculadoraPedidoPadrao(FreteProperties freteProperties, ImpostoProperties impostoProperties) {
        this.freteProperties = freteProperties;
        this.impostoProperties = impostoProperties;
    }

    @Override
    public CalculadoraPedido montar(Cupom cupom) {
        CalculadoraPedido calculadora = new CalculoPedidoBase();
        if (cupom != null) {
            calculadora = new CupomDescontoDecorator(calculadora, cupom);
        }
        List<String> prefixos = freteProperties.getPrefixosInternos();
        if (prefixos == null || prefixos.isEmpty()) {
            prefixos = List.of("74", "75");
        }
        calculadora = new FreteDecorator(
                calculadora,
                freteProperties.getValorInterno(),
                freteProperties.getValorNacional(),
                freteProperties.getLimiteGratis(),
                prefixos);
        calculadora = new ImpostoDecorator(calculadora, impostoProperties.getAliquota());
        return calculadora;
    }
}
