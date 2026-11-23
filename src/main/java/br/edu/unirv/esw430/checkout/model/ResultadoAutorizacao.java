package br.edu.unirv.esw430.checkout.model;

public class ResultadoAutorizacao {

    private final boolean autorizado;
    private final String gateway;
    private final String idTransacao;
    private final String codigoAutorizacao;
    private final String mensagem;
    private final String detalheAdaptacao;

    public ResultadoAutorizacao(boolean autorizado, String gateway, String idTransacao,
                                String codigoAutorizacao, String mensagem, String detalheAdaptacao) {
        this.autorizado = autorizado;
        this.gateway = gateway;
        this.idTransacao = idTransacao;
        this.codigoAutorizacao = codigoAutorizacao;
        this.mensagem = mensagem;
        this.detalheAdaptacao = detalheAdaptacao;
    }

    public boolean isAutorizado() {
        return autorizado;
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

    public String getMensagem() {
        return mensagem;
    }

    public String getDetalheAdaptacao() {
        return detalheAdaptacao;
    }
}
