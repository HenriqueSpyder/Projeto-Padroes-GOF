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
import br.edu.unirv.esw430.checkout.service.EmissorComprovante;
import br.edu.unirv.esw430.checkout.service.ValidadorCheckout;
import br.edu.unirv.esw430.checkout.service.ValidadorCheckout.DadosPagamento;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Facade do fechamento de pedido. Um único método de alto nível esconde a
 * consulta no repositório, a cadeia de cálculo e a autorização no gateway.
 */
@Service
public class CheckoutFacade {

    private final PedidoRepository pedidoRepository;
    private final CalculoPedidoService calculoPedidoService;
    private final ValidadorCheckout validador;
    private final EmissorComprovante emissorComprovante;
    private final List<ProcessadorPagamento> processadores;

    public CheckoutFacade(PedidoRepository pedidoRepository,
                          CalculoPedidoService calculoPedidoService,
                          ValidadorCheckout validador,
                          EmissorComprovante emissorComprovante,
                          List<ProcessadorPagamento> processadores) {
        this.pedidoRepository = pedidoRepository;
        this.calculoPedidoService = calculoPedidoService;
        this.validador = validador;
        this.emissorComprovante = emissorComprovante;
        this.processadores = List.copyOf(processadores);
    }

    public ResultadoCheckout finalizarPedido(SolicitacaoCheckout solicitacao) {
        String pedidoId = validador.exigirPedidoId(solicitacao);
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + pedidoId + "."));
        validador.exigirPedidoPronto(pedido);
        DadosPagamento dados = validador.exigirDadosPagamento(solicitacao);

        ResultadoCalculo calculo = calculoPedidoService.calcular(pedido, solicitacao.getCodigoCupom());
        if (calculo.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CheckoutInvalidoException("O valor final do pedido deve ser maior que zero.");
        }

        ProcessadorPagamento processador = selecionarProcessador(dados.gateway());
        RequisicaoPagamento requisicao = new RequisicaoPagamento(
                pedido.getId(),
                calculo.getTotal(),
                "BRL",
                dados.token(),
                pedido.getCliente().getDocumento(),
                "Checkout do pedido " + pedido.getId());
        ResultadoAutorizacao autorizacao = processador.autorizarTransacao(requisicao);

        pedido.setStatus(autorizacao.isAutorizado() ? StatusPedido.PAGO : StatusPedido.RECUSADO);
        pedidoRepository.salvar(pedido);

        if (autorizacao.isAutorizado()) {
            Comprovante comprovante = emissorComprovante.emitir(pedido, autorizacao, calculo.getTotal());
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
}
