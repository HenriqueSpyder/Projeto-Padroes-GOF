package br.edu.unirv.esw430.checkout.exception;

public class PedidoNaoEncontradoException extends RuntimeException {

    public PedidoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
