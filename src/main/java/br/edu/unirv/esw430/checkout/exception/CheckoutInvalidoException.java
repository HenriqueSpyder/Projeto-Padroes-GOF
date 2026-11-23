package br.edu.unirv.esw430.checkout.exception;

public class CheckoutInvalidoException extends RuntimeException {

    public CheckoutInvalidoException(String mensagem) {
        super(mensagem);
    }
}
