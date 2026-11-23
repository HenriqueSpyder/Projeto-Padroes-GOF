package br.edu.unirv.esw430.checkout.config;

import br.edu.unirv.esw430.checkout.exception.CheckoutInvalidoException;
import br.edu.unirv.esw430.checkout.exception.PedidoNaoEncontradoException;
import br.edu.unirv.esw430.checkout.model.Comprovante;
import br.edu.unirv.esw430.checkout.model.LinhaCalculo;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.RequisicaoPagamento;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;
import br.edu.unirv.esw430.checkout.model.ResultadoCheckout;
import br.edu.unirv.esw430.checkout.model.SolicitacaoCheckout;
import br.edu.unirv.esw430.checkout.patterns.adapter.ProcessadorGatewayA;
import br.edu.unirv.esw430.checkout.patterns.adapter.ProcessadorGatewayB;
import br.edu.unirv.esw430.checkout.patterns.adapter.ProcessadorPagamento;
import br.edu.unirv.esw430.checkout.patterns.facade.CheckoutFacade;
import br.edu.unirv.esw430.checkout.service.CalculoPedidoService;
import br.edu.unirv.esw430.checkout.service.PedidoConsultaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Visão de console. Roda na inicialização e mostra Adapter, Decorator e Facade
 * com os pedidos de demonstração.
 */
@Component
@ConditionalOnProperty(prefix = "checkout.demo", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemonstracaoCheckoutRunner implements CommandLineRunner {

    private final List<ProcessadorPagamento> processadores;
    private final CalculoPedidoService calculoPedidoService;
    private final PedidoConsultaService pedidoConsultaService;
    private final CheckoutFacade checkoutFacade;

    public DemonstracaoCheckoutRunner(List<ProcessadorPagamento> processadores,
                                      CalculoPedidoService calculoPedidoService,
                                      PedidoConsultaService pedidoConsultaService,
                                      CheckoutFacade checkoutFacade) {
        this.processadores = processadores;
        this.calculoPedidoService = calculoPedidoService;
        this.pedidoConsultaService = pedidoConsultaService;
        this.checkoutFacade = checkoutFacade;
    }

    @Override
    public void run(String... args) {
        secao("DEMONSTRAÇÃO — CHECKOUT ESW430 — ADAPTER, DECORATOR E FACADE");
        System.out.println("Os SDKs são simulados. Token com o texto RECUS é recusado.");
        System.out.println("Gateway B também recusa valores acima de R$ 5.000,00.");
        System.out.println("PED-1005 permanece aberto para teste pela interface web ou pela API.");

        secao("1. ADAPTER — ProcessadorPagamento traduz SDKs incompatíveis");
        cenario("Gateway A aprova R$ 10,50 e converte o valor para 1050 centavos", () -> {
            ResultadoAutorizacao resultado = gateway(ProcessadorGatewayA.NOME).autorizarTransacao(
                    requisicao("PED-X", "10.50", "tok_visa_4242", "12345678901"));
            imprimirAutorizacao(resultado);
        });
        cenario("Gateway A recusa o token tok_recusado (status DECLINED do SDK)", () -> {
            ResultadoAutorizacao resultado = gateway(ProcessadorGatewayA.NOME).autorizarTransacao(
                    requisicao("PED-X", "10.50", "tok_recusado", "12345678901"));
            imprimirAutorizacao(resultado);
        });
        cenario("Gateway A recebe token vazio: a exceção do SDK vira resposta do domínio", () -> {
            ResultadoAutorizacao resultado = gateway(ProcessadorGatewayA.NOME).autorizarTransacao(
                    requisicao("PED-X", "10.50", "", "12345678901"));
            imprimirAutorizacao(resultado);
        });
        cenario("Gateway B aprova e traduz codigoRetorno 0", () -> {
            ResultadoAutorizacao resultado = gateway(ProcessadorGatewayB.NOME).autorizarTransacao(
                    requisicao("PED-Y", "89.90", "tok_master_5555", "98765432100"));
            imprimirAutorizacao(resultado);
        });
        cenario("Gateway B recusa valor acima do limite (codigoRetorno 51)", () -> {
            ResultadoAutorizacao resultado = gateway(ProcessadorGatewayB.NOME).autorizarTransacao(
                    requisicao("PED-Y", "5000.01", "tok_master_5555", "98765432100"));
            imprimirAutorizacao(resultado);
        });

        secao("2. DECORATOR — cadeia de CalculadoraPedido");
        cenarioCalculo("PED-1001 com PROMO10 (frete regional)", "PED-1001", "PROMO10");
        cenarioCalculo("PED-1002 sem cupom (frete nacional)", "PED-1002", null);
        cenarioCalculo("PED-1003 com VALE20 (frete grátis acima de R$ 300,00)", "PED-1003", "VALE20");
        cenarioCalculo("PED-1005 com BEMVINDO", "PED-1005", "BEMVINDO");

        secao("3. FACADE — CheckoutFacade.finalizarPedido orquestra o fechamento");
        cenarioFacade("PED-1001 pago no Gateway A com PROMO10",
                new SolicitacaoCheckout("PED-1001", "GATEWAY_A", "PROMO10", "tok_visa_4242"));
        cenarioFacade("PED-1002 pago no Gateway B sem cupom",
                new SolicitacaoCheckout("PED-1002", "GATEWAY_B", null, "tok_master_5555"));
        cenarioFacade("PED-1003 recusado no Gateway A",
                new SolicitacaoCheckout("PED-1003", "GATEWAY_A", "VALE20", "tok_recusado"));
        cenarioFacade("PED-1004 já estava pago",
                new SolicitacaoCheckout("PED-1004", "GATEWAY_B", null, "tok_visa_4242"));
        cenarioFacade("Pedido inexistente",
                new SolicitacaoCheckout("PED-9999", "GATEWAY_A", null, "tok_visa_4242"));
        cenarioFacade("PED-1005 com cupom inexistente (o pedido continua aberto)",
                new SolicitacaoCheckout("PED-1005", "GATEWAY_A", "NAOEXISTE", "tok_visa_4242"));

        secao("SITUAÇÃO FINAL DOS PEDIDOS EM MEMÓRIA");
        for (Pedido pedido : pedidoConsultaService.listar()) {
            System.out.println("  " + pedido.getId() + " | " + pedido.getCliente().getNome()
                    + " | " + pedido.getStatus() + " | CEP " + pedido.getCepEntrega());
        }
        System.out.println();
        System.out.println("Interface web: http://localhost:8080/");
        System.out.println("API de pedidos: http://localhost:8080/api/pedidos");
        System.out.println("Checkout manual sugerido: PED-1005, GATEWAY_A, cupom PROMO10, token tok_visa_4242");
        System.out.println("Para repetir os cenários sem reiniciar, use Restaurar na página inicial.");
        System.out.println("=".repeat(72));
    }

    private void cenarioCalculo(String titulo, String pedidoId, String cupom) {
        cenario(titulo, () -> {
            Pedido pedido = pedidoConsultaService.buscar(pedidoId);
            System.out.println("  Cadeia ..........: " + calculoPedidoService.descreverCadeia(cupom));
            imprimirCalculo(calculoPedidoService.calcular(pedido, cupom));
        });
    }

    private void cenarioFacade(String titulo, SolicitacaoCheckout solicitacao) {
        cenario(titulo, () -> imprimirCheckout(checkoutFacade.finalizarPedido(solicitacao)));
    }

    private void cenario(String titulo, Runnable acao) {
        System.out.println();
        System.out.println("--- " + titulo);
        try {
            acao.run();
        } catch (PedidoNaoEncontradoException | CheckoutInvalidoException ex) {
            System.out.println("  Resultado esperado (regra de negócio): " + ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("  ERRO inesperado: " + ex.getClass().getSimpleName() + " — " + ex.getMessage());
        }
    }

    private void imprimirCheckout(ResultadoCheckout resultado) {
        System.out.println("  Confirmado ......: " + simNao(resultado.isConfirmado()));
        System.out.println("  Mensagem ........: " + resultado.getMensagem());
        System.out.println("  Pedido ..........: " + resultado.getPedidoId()
                + " | " + resultado.getClienteNome()
                + " | status " + resultado.getStatusPedido());
        imprimirCalculo(resultado.getCalculo());
        imprimirAutorizacao(resultado.getAutorizacao());
        Comprovante comprovante = resultado.getComprovante();
        if (comprovante == null) {
            System.out.println("  Comprovante .....: não emitido");
            return;
        }
        System.out.println("  Comprovante .....: " + comprovante.getNumero()
                + " | " + comprovante.getGateway()
                + " | " + comprovante.getIdTransacao()
                + " | " + moeda(comprovante.getValorTotal()));
    }

    private void imprimirAutorizacao(ResultadoAutorizacao resultado) {
        System.out.println("  Autorizado ......: " + simNao(resultado.isAutorizado()));
        System.out.println("  Gateway .........: " + resultado.getGateway());
        System.out.println("  Id transação ....: " + nvl(resultado.getIdTransacao()));
        System.out.println("  Código auth .....: " + nvl(resultado.getCodigoAutorizacao()));
        System.out.println("  Mensagem ........: " + resultado.getMensagem());
        System.out.println("  Adaptação .......: " + resultado.getDetalheAdaptacao());
    }

    private void imprimirCalculo(ResultadoCalculo calculo) {
        for (LinhaCalculo linha : calculo.getLinhas()) {
            System.out.printf("  %-58s %14s%n", linha.getDescricao(), moeda(linha.getValor()));
        }
        System.out.printf("  %-58s %14s%n", "TOTAL", moeda(calculo.getTotal()));
    }

    private ProcessadorPagamento gateway(String nome) {
        return processadores.stream()
                .filter(processador -> processador.getNomeGateway().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Gateway não registrado: " + nome));
    }

    private RequisicaoPagamento requisicao(String referencia, String valor, String token, String documento) {
        return new RequisicaoPagamento(referencia, new BigDecimal(valor), "BRL", token, documento, "demonstração");
    }

    private String moeda(BigDecimal valor) {
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR"));
        DecimalFormat formato = new DecimalFormat("#,##0.00", simbolos);
        return "R$ " + formato.format(valor);
    }

    private String simNao(boolean valor) {
        return valor ? "sim" : "não";
    }

    private String nvl(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor;
    }

    private void secao(String titulo) {
        System.out.println();
        System.out.println("=".repeat(72));
        System.out.println(titulo);
        System.out.println("=".repeat(72));
    }
}
