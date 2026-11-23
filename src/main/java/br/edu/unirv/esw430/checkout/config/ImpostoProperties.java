package br.edu.unirv.esw430.checkout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "checkout.imposto")
public class ImpostoProperties {

    private BigDecimal aliquota = new BigDecimal("0.08");

    public static ImpostoProperties padrao() {
        return new ImpostoProperties();
    }

    public BigDecimal getAliquota() {
        return aliquota;
    }

    public void setAliquota(BigDecimal aliquota) {
        this.aliquota = aliquota;
    }
}
