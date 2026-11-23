package br.edu.unirv.esw430.checkout.controller;

import br.edu.unirv.esw430.checkout.controller.dto.SolicitacaoCheckoutForm;
import br.edu.unirv.esw430.checkout.exception.CheckoutInvalidoException;
import br.edu.unirv.esw430.checkout.exception.PedidoNaoEncontradoException;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCheckout;
import br.edu.unirv.esw430.checkout.patterns.adapter.ProcessadorGatewayA;
import br.edu.unirv.esw430.checkout.patterns.adapter.ProcessadorPagamento;
import br.edu.unirv.esw430.checkout.patterns.decorator.CalculadoraPedido;
import br.edu.unirv.esw430.checkout.patterns.facade.CheckoutFacade;
import br.edu.unirv.esw430.checkout.service.CalculoPedidoService;
import br.edu.unirv.esw430.checkout.service.PedidoConsultaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CheckoutMvcController {

    private final PedidoConsultaService pedidoConsultaService;
    private final CalculoPedidoService calculoPedidoService;
    private final CheckoutFacade checkoutFacade;
    private final List<ProcessadorPagamento> processadores;

    public CheckoutMvcController(PedidoConsultaService pedidoConsultaService,
                                 CalculoPedidoService calculoPedidoService,
                                 CheckoutFacade checkoutFacade,
                                 List<ProcessadorPagamento> processadores) {
        this.pedidoConsultaService = pedidoConsultaService;
        this.calculoPedidoService = calculoPedidoService;
        this.checkoutFacade = checkoutFacade;
        this.processadores = processadores;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("form")) {
            SolicitacaoCheckoutForm form = new SolicitacaoCheckoutForm();
            form.setPedidoId("PED-1005");
            form.setGateway(ProcessadorGatewayA.NOME);
            form.setTokenCartao("tok_visa_4242");
            model.addAttribute("form", form);
        }
        popularFormulario(model);
        return "index";
    }

    @PostMapping("/checkout")
    public String checkout(@ModelAttribute("form") @Valid SolicitacaoCheckoutForm form,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errosForm", bindingResult.getAllErrors().stream()
                    .map(erro -> erro.getDefaultMessage())
                    .toList());
            popularFormulario(model);
            return "index";
        }
        try {
            ResultadoCheckout resultado = checkoutFacade.finalizarPedido(form.paraSolicitacao());
            model.addAttribute("resultado", resultado);
            return "resultado";
        } catch (PedidoNaoEncontradoException | CheckoutInvalidoException ex) {
            model.addAttribute("erro", ex.getMessage());
            popularFormulario(model);
            return "index";
        }
    }

    @GetMapping("/pedidos/{id}/calculo")
    public String calculo(@PathVariable String id,
                          @RequestParam(name = "cupom", required = false) String cupom,
                          Model model) {
        try {
            Pedido pedido = pedidoConsultaService.buscar(id);
            CalculadoraPedido calculadora = calculoPedidoService.obterCalculadora(cupom);
            model.addAttribute("pedido", pedido);
            model.addAttribute("cupom", cupom);
            model.addAttribute("cadeia", calculoPedidoService.descrever(calculadora));
            model.addAttribute("calculo", calculadora.calcular(pedido));
            return "calculo";
        } catch (PedidoNaoEncontradoException | CheckoutInvalidoException ex) {
            model.addAttribute("erro", ex.getMessage());
            return "erro";
        }
    }

    @PostMapping("/pedidos/restaurar")
    public String restaurar(RedirectAttributes redirectAttributes) {
        pedidoConsultaService.restaurarDadosDemonstracao();
        redirectAttributes.addFlashAttribute("aviso", "Pedidos de demonstração restaurados.");
        return "redirect:/";
    }

    private void popularFormulario(Model model) {
        model.addAttribute("pedidos", pedidoConsultaService.listar());
        model.addAttribute("cupons", pedidoConsultaService.listarCupons());
        model.addAttribute("gateways", processadores.stream()
                .sorted((a, b) -> a.getNomeGateway().compareTo(b.getNomeGateway()))
                .toList());
    }
}
