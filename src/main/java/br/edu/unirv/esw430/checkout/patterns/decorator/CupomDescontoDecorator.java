package br.edu.unirv.esw430.checkout.patterns.decorator;

import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.model.Dinheiro;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;
import br.edu.unirv.esw430.checkout.model.TipoCupom;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decorator de desconto. Aplica cupom percentual ou de valor fixo sobre o subtotal.
 */
public class CupomDescontoDecorator extends CalculadoraDecorator {

    private final Cupom cupom;

    public CupomDescontoDecorator(CalculadoraPedido envolvida, Cupom cupom) {
        super(envolvida);
        if (cupom == null) {
            throw new IllegalArgumentException("Cupom é obrigatório neste decorador.");
        }
        this.cupom = cupom;
    }

    @Override
    public ResultadoCalculo calcular(Pedido pedido) {
        ResultadoCalculo parcial = getComponente().calcular(pedido);
        BigDecimal desconto;
        String descricao;
        if (cupom.getTipo() == TipoCupom.PERCENTUAL) {
            desconto = parcial.getSubtotal()
                    .multiply(cupom.getValor())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            descricao = "Cupom " + cupom.getCodigo() + " ("
                    + cupom.getValor().stripTrailingZeros().toPlainString()
                    + "% sobre o subtotal)";
        } else if (cupom.getTipo() == TipoCupom.VALOR_FIXO) {
            desconto = cupom.getValor();
            descricao = "Cupom " + cupom.getCodigo() + " (valor fixo)";
        } else {
            throw new IllegalStateException("Tipo de cupom não suportado: " + cupom.getTipo());
        }
        if (desconto.compareTo(parcial.getSubtotal()) > 0) {
            desconto = parcial.getSubtotal();
            descricao = descricao + " (limitado ao subtotal)";
        }
        if (desconto.signum() < 0) {
            desconto = Dinheiro.ZERO;
        }
        return parcial.comDesconto(desconto, descricao);
    }

    public Cupom getCupom() {
        return cupom;
    }
}
