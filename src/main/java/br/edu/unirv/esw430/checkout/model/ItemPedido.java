package br.edu.unirv.esw430.checkout.model;

import java.math.BigDecimal;

public class ItemPedido {

    private final String sku;
    private final String descricao;
    private final int quantidade;
    private final BigDecimal precoUnitario;

    public ItemPedido(String sku, String descricao, int quantidade, BigDecimal precoUnitario) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU do item é obrigatório.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição do item é obrigatória.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        }
        if (precoUnitario == null || precoUnitario.signum() < 0) {
            throw new IllegalArgumentException("Preço unitário inválido.");
        }
        this.sku = sku.trim();
        this.descricao = descricao.trim();
        this.quantidade = quantidade;
        this.precoUnitario = Dinheiro.arredondar(precoUnitario);
    }

    public String getSku() {
        return sku;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
}
