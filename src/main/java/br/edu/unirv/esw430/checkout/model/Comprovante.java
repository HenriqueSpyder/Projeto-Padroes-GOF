package br.edu.unirv.esw430.checkout.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Comprovante {

    private final String numero;
    private final String pedidoId;
    private final String clienteNome;
    private final String gateway;
    private final String idTransacao;
    private final String codigoAutorizacao;
    private final BigDecimal valorTotal;
    private final LocalDateTime emitidoEm;

    public Comprovante(String numero, String pedidoId, String clienteNome, String gateway,
                       String idTransacao, String codigoAutorizacao, BigDecimal valorTotal,
                       LocalDateTime emitidoEm) {
        this.numero = numero;
        this.pedidoId = pedidoId;
        this.clienteNome = clienteNome;
        this.gateway = gateway;
        this.idTransacao = idTransacao;
        this.codigoAutorizacao = codigoAutorizacao;
        this.valorTotal = valorTotal;
        this.emitidoEm = emitidoEm;
    }

    public String getNumero() {
        return numero;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public String getGateway() {
        return gateway;
    }

    public String getIdTransacao() {
        return idTransacao;
    }

    public String getCodigoAutorizacao() {
        return codigoAutorizacao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getEmitidoEm() {
        return emitidoEm;
    }
}
