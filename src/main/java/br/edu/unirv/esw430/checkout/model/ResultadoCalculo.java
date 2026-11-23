package br.edu.unirv.esw430.checkout.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ResultadoCalculo {

    private final BigDecimal subtotal;
    private final BigDecimal desconto;
    private final BigDecimal frete;
    private final BigDecimal imposto;
    private final BigDecimal total;
    private final List<LinhaCalculo> linhas;

    private ResultadoCalculo(BigDecimal subtotal, BigDecimal desconto, BigDecimal frete,
                             BigDecimal imposto, List<LinhaCalculo> linhas) {
        this.subtotal = Dinheiro.arredondar(subtotal);
        this.desconto = Dinheiro.arredondar(desconto);
        this.frete = Dinheiro.arredondar(frete);
        this.imposto = Dinheiro.arredondar(imposto);
        this.linhas = new ArrayList<>(linhas);
        this.total = Dinheiro.arredondar(
                this.subtotal.subtract(this.desconto).add(this.frete).add(this.imposto));
    }

    public static ResultadoCalculo doSubtotal(BigDecimal subtotal) {
        BigDecimal valor = Dinheiro.arredondar(subtotal);
        return new ResultadoCalculo(
                valor,
                Dinheiro.ZERO,
                Dinheiro.ZERO,
                Dinheiro.ZERO,
                List.of(new LinhaCalculo("Subtotal dos itens", valor)));
    }

    public ResultadoCalculo comDesconto(BigDecimal desconto, String descricao) {
        BigDecimal valor = limitarDesconto(Dinheiro.arredondar(desconto));
        List<LinhaCalculo> novasLinhas = copiarLinhas();
        novasLinhas.add(new LinhaCalculo(descricao, valor.negate()));
        return new ResultadoCalculo(subtotal, valor, frete, imposto, novasLinhas);
    }

    public ResultadoCalculo comFrete(BigDecimal frete, String descricao) {
        BigDecimal valor = naoNegativo(Dinheiro.arredondar(frete));
        List<LinhaCalculo> novasLinhas = copiarLinhas();
        novasLinhas.add(new LinhaCalculo(descricao, valor));
        return new ResultadoCalculo(subtotal, desconto, valor, imposto, novasLinhas);
    }

    public ResultadoCalculo comImposto(BigDecimal imposto, String descricao) {
        BigDecimal valor = naoNegativo(Dinheiro.arredondar(imposto));
        List<LinhaCalculo> novasLinhas = copiarLinhas();
        novasLinhas.add(new LinhaCalculo(descricao, valor));
        return new ResultadoCalculo(subtotal, desconto, frete, valor, novasLinhas);
    }

    private BigDecimal limitarDesconto(BigDecimal desconto) {
        if (desconto.signum() < 0) {
            return Dinheiro.ZERO;
        }
        if (desconto.compareTo(subtotal) > 0) {
            return subtotal;
        }
        return desconto;
    }

    private BigDecimal naoNegativo(BigDecimal valor) {
        return valor.signum() < 0 ? Dinheiro.ZERO : valor;
    }

    private List<LinhaCalculo> copiarLinhas() {
        return new ArrayList<>(linhas);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public BigDecimal getImposto() {
        return imposto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<LinhaCalculo> getLinhas() {
        return List.copyOf(linhas);
    }
}
