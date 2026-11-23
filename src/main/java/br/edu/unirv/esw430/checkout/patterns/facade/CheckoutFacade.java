package br.edu.unirv.esw430.checkout.patterns.facade;

import br.edu.unirv.esw430.checkout.exception.CheckoutInvalidoException;
import br.edu.unirv.esw430.checkout.exception.PedidoNaoEncontradoException;
import br.edu.unirv.esw430.checkout.model.Comprovante;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.RequisicaoPagamento;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;
import br.edu.unirv.esw430.checkout.model.ResultadoCheckout;
import br.edu.unirv.esw430.checkout.model.SolicitacaoCheckout;
import br.edu.unirv.esw430.checkout.model.StatusPedido;
import br.edu.unirv.esw430.checkout.patterns.adapter.ProcessadorPagamento;
import br.edu.unirv.esw430.checkout.repository.PedidoRepository;
import br.edu.unirv.esw430.checkout.service.CalculoPedidoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Facade do fechamento de pedido. Um único método de alto nível esconde a
 * consulta no repositório, a cadeia de cálculo e a autorização no gateway.
 */
@Service
public class CheckoutFacade {

    private static final DateTimeFormatter NUMERO_COMPROVANTE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PedidoRepository pedidoRepository;
    private final CalculoPedidoService calculoPedidoService;
    private final List<ProcessadorPagamento> processadores;

    public CheckoutFacade(PedidoRepository pedidoRepository,
                          CalculoPedidoService calculoPedidoService,
                          List<ProcessadorPagamento> processadores) {
        this.pedidoRepository = pedidoRepository;
        this.calculoPedidoService = calculoPedidoService;
        this.processadores = List.copyOf(processadores);
    }

    public ResultadoCheckout finalizarPedido(SolicitacaoCheckout solicitacao) {
        if (solicitacao == null) {
            throw new CheckoutInvalidoException("Solicitação de checkout é obrigatória.");
        }

        String pedidoId = texto(solicitacao.getPedidoId());
        if (pedidoId.isBlank()) {
            throw new CheckoutInvalidoException("Informe o identificador do pedido.");
        }

        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + pedidoId + "."));

        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new CheckoutInvalidoException(
                    "Pedido " + pedido.getId() + " não está aberto para checkout. Status atual: "
                            + pedido.getStatus().getDescricao() + ".");
        }
        if (pedido.getItens().isEmpty()) {
            throw new CheckoutInvalidoException("Pedido " + pedido.getId() + " não possui itens.");
        }

        String token = texto(solicitacao.getTokenCartao());
        if (token.isBlank()) {
            throw new CheckoutInvalidoException("Informe o token do cartão.");
        }
        String gateway = texto(solicitacao.getGateway());
        if (gateway.isBlank()) {
            throw new CheckoutInvalidoException("Informe o gateway de pagamento.");
        }

        ResultadoCalculo calculo = calculoPedidoService.calcular(pedido, solicitacao.getCodigoCupom());
        if (calculo.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CheckoutInvalidoException("O valor final do pedido deve ser maior que zero.");
        }

        ProcessadorPagamento processador = selecionarProcessador(gateway);
        RequisicaoPagamento requisicao = new RequisicaoPagamento(
                pedido.getId(),
                calculo.getTotal(),
                "BRL",
                token,
                pedido.getCliente().getDocumento(),
                "Checkout do pedido " + pedido.getId());

        ResultadoAutorizacao autorizacao = processador.autorizarTransacao(requisicao);
        if (autorizacao.isAutorizado()) {
            pedido.setStatus(StatusPedido.PAGO);
        } else {
            pedido.setStatus(StatusPedido.RECUSADO);
        }
        pedidoRepository.salvar(pedido);

        if (autorizacao.isAutorizado()) {
            Comprovante comprovante = emitirComprovante(pedido, autorizacao, calculo.getTotal());
            return new ResultadoCheckout(
                    true,
                    "Pedido finalizado e pagamento autorizado.",
                    pedido.getId(),
                    pedido.getCliente().getNome(),
                    pedido.getStatus(),
                    calculo,
                    autorizacao,
                    comprovante);
        }

        return new ResultadoCheckout(
                false,
                "Pagamento não autorizado. O pedido foi marcado como recusado.",
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getStatus(),
                calculo,
                autorizacao,
                null);
    }

    private ProcessadorPagamento selecionarProcessador(String gateway) {
        return processadores.stream()
                .filter(processador -> processador.getNomeGateway().equalsIgnoreCase(gateway))
                .findFirst()
                .orElseThrow(() -> new CheckoutInvalidoException(
                        "Gateway não suportado: " + gateway + ". Disponíveis: "
                                + String.join(", ", processadores.stream()
                                .map(ProcessadorPagamento::getNomeGateway)
                                .sorted()
                                .toList()) + "."));
    }

    private Comprovante emitirComprovante(Pedido pedido, ResultadoAutorizacao autorizacao, BigDecimal total) {
        LocalDateTime agora = LocalDateTime.now();
        String numero = "CMP-" + pedido.getId() + "-" + agora.format(NUMERO_COMPROVANTE);
        return new Comprovante(
                numero,
                pedido.getId(),
                pedido.getCliente().getNome(),
                autorizacao.getGateway(),
                autorizacao.getIdTransacao(),
                autorizacao.getCodigoAutorizacao(),
                total,
                agora);
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
