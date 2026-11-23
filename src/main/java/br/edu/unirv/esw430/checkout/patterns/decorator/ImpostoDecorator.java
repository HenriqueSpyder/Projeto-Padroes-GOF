package br.edu.unirv.esw430.checkout.patterns.decorator;

import br.edu.unirv.esw430.checkout.model.Dinheiro;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;

import java.math.BigDecimal;

/**
 * Decorator de imposto. A alíquota incide sobre subtotal menos desconto, sem incluir o frete.
 * Por isso este decorador deve ficar por fora do cupom na cadeia.
 */
public class ImpostoDecorator extends CalculadoraDecorator {

    private final BigDecimal aliquota;

    public ImpostoDecorator(CalculadoraPedido envolvida, BigDecimal aliquota) {
        super(envolvida);
        if (aliquota == null || aliquota.signum() < 0) {
            throw new IllegalArgumentException("Alíquota de imposto inválida.");
        }
        this.aliquota = aliquota;
    }

    @Override
    public ResultadoCalculo calcular(Pedido pedido) {
        ResultadoCalculo parcial = getComponente().calcular(pedido);
        BigDecimal base = parcial.getSubtotal().subtract(parcial.getDesconto());
        if (base.signum() < 0) {
            base = BigDecimal.ZERO;
        }
        BigDecimal imposto = Dinheiro.arredondar(base.multiply(aliquota));
        String percentual = aliquota.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        return parcial.comImposto(
                imposto,
                "Imposto simulado (" + percentual + "% sobre subtotal menos desconto)");
    }
}
