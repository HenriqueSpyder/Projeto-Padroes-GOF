package br.edu.unirv.esw430.checkout.patterns.adapter.sdk;

public class GatewayBRetorno {

    private final int codigoRetorno;
    private final String nsu;
    private final String mensagemOperadora;

    public GatewayBRetorno(int codigoRetorno, String nsu, String mensagemOperadora) {
        this.codigoRetorno = codigoRetorno;
        this.nsu = nsu;
        this.mensagemOperadora = mensagemOperadora;
    }

    public int getCodigoRetorno() {
        return codigoRetorno;
    }

    public String getNsu() {
        return nsu;
    }

    public String getMensagemOperadora() {
        return mensagemOperadora;
    }
}
