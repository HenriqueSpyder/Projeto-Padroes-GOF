package br.edu.unirv.esw430.checkout.model;

import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private final String id;
    private final Cliente cliente;
    private final String cepEntrega;
    private StatusPedido status;
    private final List<ItemPedido> itens;

    public Pedido(String id, Cliente cliente, String cepEntrega, StatusPedido status, List<ItemPedido> itens) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id do pedido é obrigatório.");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente é obrigatório.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório.");
        }
        this.id = id.trim();
        this.cliente = cliente;
        this.cepEntrega = cepEntrega == null ? "" : cepEntrega.trim();
        this.status = status;
        this.itens = new ArrayList<>();
        if (itens != null) {
            for (ItemPedido item : itens) {
                if (item == null) {
                    throw new IllegalArgumentException("Item do pedido não pode ser nulo.");
                }
                this.itens.add(item);
            }
        }
    }

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getCepEntrega() {
        return cepEntrega;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório.");
        }
        this.status = status;
    }

    public List<ItemPedido> getItens() {
        return List.copyOf(itens);
    }
}
