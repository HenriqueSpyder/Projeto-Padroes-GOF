package br.edu.unirv.esw430.checkout.patterns.adapter.sdk;

/**
 * Exceção do SDK simulado do Gateway A. O vocabulário é o da biblioteca externa,
 * não o do domínio de checkout.
 */
public class GatewayASdkException extends RuntimeException {

    public GatewayASdkException(String message) {
        super(message);
    }
}
