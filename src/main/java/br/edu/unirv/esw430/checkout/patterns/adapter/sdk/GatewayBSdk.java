package br.edu.unirv.esw430.checkout.patterns.adapter.sdk;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * SDK simulado e incompatível com o domínio. Recebe double, documento e devolve código numérico.
 */
@Component
public class GatewayBSdk {

    public GatewayBRetorno pagar(String contaLoja, double valorReais, String documentoCliente,
                                 String referenciaPedido, String meioPagamento) {
        if (contaLoja == null || contaLoja.isBlank()) {
            throw new GatewayBSdkException("conta da loja invalida");
        }
        if (referenciaPedido == null || referenciaPedido.isBlank()) {
            throw new GatewayBSdkException("referencia ausente");
        }
        String documento = documentoCliente == null ? "" : documentoCliente.trim();
        if (documento.length() > 20) {
            throw new GatewayBSdkException("documento invalido");
        }
        if (valorReais <= 0.0d) {
            throw new GatewayBSdkException("valor invalido");
        }
        boolean recusado = meioPagamento != null && meioPagamento.toUpperCase(Locale.ROOT).contains("RECUS");
        boolean acimaDoLimite = valorReais > 5000.0d;
        String nsu = "NSU-B-" + referenciaPedido;
        if (recusado || acimaDoLimite) {
            String operadora = acimaDoLimite ? "LIMITE_EXCEDIDO" : "NAO_AUTORIZADO";
            return new GatewayBRetorno(51, nsu, operadora);
        }
        return new GatewayBRetorno(0, nsu, "AUTORIZADO");
    }
}
