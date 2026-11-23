package br.edu.unirv.esw430.checkout.patterns.decorator;

import br.edu.unirv.esw430.checkout.model.Dinheiro;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Decorator de frete. Frete grátis a partir do limite, valor regional por prefixo de CEP
 * e valor nacional nos demais casos. A regra usa o subtotal dos itens, antes do desconto.
 */
public class FreteDecorator extends CalculadoraDecorator {

    private final BigDecimal valorInterno;
    private final BigDecimal valorNacional;
    private final BigDecimal limiteGratis;
    private final List<String> prefixosInternos;

    public FreteDecorator(CalculadoraPedido envolvida, BigDecimal valorInterno, BigDecimal valorNacional,
                          BigDecimal limiteGratis, List<String> prefixosInternos) {
        super(envolvida);
        this.valorInterno = valorInterno == null ? new BigDecimal("15.00") : valorInterno;
        this.valorNacional = valorNacional == null ? new BigDecimal("29.90") : valorNacional;
        this.limiteGratis = limiteGratis == null ? new BigDecimal("300.00") : limiteGratis;
        this.prefixosInternos = prefixosInternos == null ? List.of("74", "75") : List.copyOf(prefixosInternos);
    }

    @Override
    public ResultadoCalculo calcular(Pedido pedido) {
        ResultadoCalculo parcial = getComponente().calcular(pedido);
        if (parcial.getSubtotal().compareTo(limiteGratis) >= 0) {
            return parcial.comFrete(
                    Dinheiro.ZERO,
                    "Frete grátis (subtotal a partir de R$ " + Dinheiro.arredondar(limiteGratis).toPlainString() + ")");
        }
        String cep = apenasDigitos(pedido.getCepEntrega());
        boolean interno = prefixosInternos.stream()
                .filter(prefixo -> prefixo != null && !prefixo.isBlank())
                .anyMatch(prefixo -> cep.startsWith(prefixo.trim()));
        if (interno) {
            return parcial.comFrete(valorInterno, "Frete regional (CEP com prefixo interno)");
        }
        return parcial.comFrete(valorNacional, "Frete nacional");
    }

    private String apenasDigitos(String cep) {
        if (cep == null) {
            return "";
        }
        return cep.replaceAll("\\D", "");
    }
}
