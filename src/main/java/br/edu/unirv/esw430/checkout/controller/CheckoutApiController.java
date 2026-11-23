package br.edu.unirv.esw430.checkout.controller;

import br.edu.unirv.esw430.checkout.controller.dto.SolicitacaoCheckoutForm;
import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;
import br.edu.unirv.esw430.checkout.model.ResultadoCheckout;
import br.edu.unirv.esw430.checkout.patterns.adapter.ProcessadorPagamento;
import br.edu.unirv.esw430.checkout.patterns.facade.CheckoutFacade;
import br.edu.unirv.esw430.checkout.service.CalculoPedidoService;
import br.edu.unirv.esw430.checkout.service.PedidoConsultaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CheckoutApiController {

    private final PedidoConsultaService pedidoConsultaService;
    private final CalculoPedidoService calculoPedidoService;
    private final CheckoutFacade checkoutFacade;
    private final List<ProcessadorPagamento> processadores;

    public CheckoutApiController(PedidoConsultaService pedidoConsultaService,
                                 CalculoPedidoService calculoPedidoService,
                                 CheckoutFacade checkoutFacade,
                                 List<ProcessadorPagamento> processadores) {
        this.pedidoConsultaService = pedidoConsultaService;
        this.calculoPedidoService = calculoPedidoService;
        this.checkoutFacade = checkoutFacade;
        this.processadores = processadores;
    }

    @GetMapping("/pedidos")
    public List<Pedido> listarPedidos() {
        return pedidoConsultaService.listar();
    }

    @GetMapping("/pedidos/{id}")
    public Pedido buscarPedido(@PathVariable String id) {
        return pedidoConsultaService.buscar(id);
    }

    @GetMapping("/pedidos/{id}/calculo")
    public ResultadoCalculo calcular(@PathVariable String id,
                                     @RequestParam(name = "cupom", required = false) String cupom) {
        Pedido pedido = pedidoConsultaService.buscar(id);
        return calculoPedidoService.calcular(pedido, cupom);
    }

    @GetMapping("/cupons")
    public List<Cupom> listarCupons() {
        return pedidoConsultaService.listarCupons();
    }

    @GetMapping("/gateways")
    public List<String> listarGateways() {
        return processadores.stream()
                .map(ProcessadorPagamento::getNomeGateway)
                .sorted()
                .toList();
    }

    @PostMapping("/checkout")
    public ResultadoCheckout checkout(@Valid @RequestBody SolicitacaoCheckoutForm form) {
        return checkoutFacade.finalizarPedido(form.paraSolicitacao());
    }

    @PostMapping("/pedidos/restaurar")
    public List<Pedido> restaurar() {
        return pedidoConsultaService.restaurarDadosDemonstracao();
    }
}
