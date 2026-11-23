package br.edu.unirv.esw430.checkout.model;

import java.math.BigDecimal;
import java.util.Locale;

public class Cupom {

    private final String codigo;
    private final TipoCupom tipo;
    private final BigDecimal valor;
    private final String descricao;

    public Cupom(String codigo, TipoCupom tipo, BigDecimal valor, String descricao) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do cupom é obrigatório.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do cupom é obrigatório.");
        }
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("Valor do cupom inválido.");
        }
        if (tipo == TipoCupom.PERCENTUAL && valor.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentual do cupom deve estar entre 0 e 100.");
        }
        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.tipo = tipo;
        this.valor = tipo == TipoCupom.PERCENTUAL
                ? new BigDecimal(valor.stripTrailingZeros().toPlainString())
                : Dinheiro.arredondar(valor);
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoCupom getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }
}
