package br.edu.unirv.esw430.checkout.controller.dto;

public class ErroResposta {

    private final String mensagem;
    private final int status;

    public ErroResposta(String mensagem, int status) {
        this.mensagem = mensagem;
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public int getStatus() {
        return status;
    }
}
