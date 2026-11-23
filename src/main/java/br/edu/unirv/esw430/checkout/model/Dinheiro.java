package br.edu.unirv.esw430.checkout.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Dinheiro {

    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private Dinheiro() {
    }

    public static BigDecimal arredondar(BigDecimal valor) {
        if (valor == null) {
            return ZERO;
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
