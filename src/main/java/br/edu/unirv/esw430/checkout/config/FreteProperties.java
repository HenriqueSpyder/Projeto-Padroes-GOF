package br.edu.unirv.esw430.checkout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "checkout.frete")
public class FreteProperties {

    private BigDecimal valorInterno = new BigDecimal("15.00");
    private BigDecimal valorNacional = new BigDecimal("29.90");
    private BigDecimal limiteGratis = new BigDecimal("300.00");
    private List<String> prefixosInternos = new ArrayList<>(List.of("74", "75"));

    public static FreteProperties padrao() {
        return new FreteProperties();
    }

    public BigDecimal getValorInterno() {
        return valorInterno;
    }

    public void setValorInterno(BigDecimal valorInterno) {
        this.valorInterno = valorInterno;
    }

    public BigDecimal getValorNacional() {
        return valorNacional;
    }

    public void setValorNacional(BigDecimal valorNacional) {
        this.valorNacional = valorNacional;
    }

    public BigDecimal getLimiteGratis() {
        return limiteGratis;
    }

    public void setLimiteGratis(BigDecimal limiteGratis) {
        this.limiteGratis = limiteGratis;
    }

    public List<String> getPrefixosInternos() {
        return prefixosInternos;
    }

    public void setPrefixosInternos(List<String> prefixosInternos) {
        this.prefixosInternos = prefixosInternos;
    }
}
