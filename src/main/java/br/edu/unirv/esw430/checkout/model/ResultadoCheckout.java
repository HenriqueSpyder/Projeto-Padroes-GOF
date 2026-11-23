package br.edu.unirv.esw430.checkout.model;

public class ResultadoCheckout {

    private final boolean confirmado;
    private final String mensagem;
    private final String pedidoId;
    private final String clienteNome;
    private final StatusPedido statusPedido;
    private final ResultadoCalculo calculo;
    private final ResultadoAutorizacao autorizacao;
    private final Comprovante comprovante;

    public ResultadoCheckout(boolean confirmado, String mensagem, String pedidoId, String clienteNome,
                             StatusPedido statusPedido, ResultadoCalculo calculo,
                             ResultadoAutorizacao autorizacao, Comprovante comprovante) {
        this.confirmado = confirmado;
        this.mensagem = mensagem;
        this.pedidoId = pedidoId;
        this.clienteNome = clienteNome;
        this.statusPedido = statusPedido;
        this.calculo = calculo;
        this.autorizacao = autorizacao;
        this.comprovante = comprovante;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public StatusPedido getStatusPedido() {
        return statusPedido;
    }

    public ResultadoCalculo getCalculo() {
        return calculo;
    }

    public ResultadoAutorizacao getAutorizacao() {
        return autorizacao;
    }

    public Comprovante getComprovante() {
        return comprovante;
    }
}
