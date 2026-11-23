package br.edu.unirv.esw430.checkout.controller;

import br.edu.unirv.esw430.checkout.patterns.adapter.ProcessadorPagamento;
import br.edu.unirv.esw430.checkout.service.PedidoConsultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CheckoutWebIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoConsultaService pedidoConsultaService;

    @Autowired
    private List<ProcessadorPagamento> processadores;

    @BeforeEach
    void restaurar() {
        pedidoConsultaService.restaurarDadosDemonstracao();
    }

    @Test
    @DisplayName("o contexto registra os dois adapters e a página inicial lista o pedido aberto")
    void paginaInicialEAdaptadores() throws Exception {
        assertThat(processadores).extracting(ProcessadorPagamento::getNomeGateway)
                .containsExactlyInAnyOrder("GATEWAY_A", "GATEWAY_B");

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PED-1005")))
                .andExpect(content().string(containsString("Adapter")));
    }

    @Test
    @DisplayName("o cálculo HTTP devolve a composição do Decorator")
    void calculoJsonEPagina() throws Exception {
        mockMvc.perform(get("/api/pedidos/PED-1001/calculo").param("cupom", "PROMO10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(closeTo(250.00, 0.001)))
                .andExpect(jsonPath("$.desconto").value(closeTo(25.00, 0.001)))
                .andExpect(jsonPath("$.frete").value(closeTo(15.00, 0.001)))
                .andExpect(jsonPath("$.imposto").value(closeTo(18.00, 0.001)))
                .andExpect(jsonPath("$.total").value(closeTo(258.00, 0.001)));

        mockMvc.perform(get("/pedidos/PED-1001/calculo").param("cupom", "PROMO10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CupomDescontoDecorator")))
                .andExpect(content().string(containsString("ImpostoDecorator")));
    }

    @Test
    @DisplayName("a API finaliza PED-1005 e a segunda tentativa é rejeitada")
    void checkoutApi() throws Exception {
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pedidoId":"PED-1005","gateway":"GATEWAY_A","codigoCupom":"PROMO10","tokenCartao":"tok_visa_4242"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmado").value(true))
                .andExpect(jsonPath("$.autorizacao.gateway").value("GATEWAY_A"))
                .andExpect(jsonPath("$.calculo.total").value(closeTo(340.20, 0.001)))
                .andExpect(jsonPath("$.comprovante.numero").exists());

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pedidoId":"PED-1005","gateway":"GATEWAY_A","tokenCartao":"tok_visa_4242"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("não está aberto")));
    }

    @Test
    @DisplayName("o formulário MVC mostra a tradução do Gateway B")
    void checkoutFormulario() throws Exception {
        mockMvc.perform(post("/checkout")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("pedidoId", "PED-1005")
                        .param("gateway", "GATEWAY_B")
                        .param("codigoCupom", "")
                        .param("tokenCartao", "tok_visa_4242"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pedido finalizado e pagamento autorizado.")))
                .andExpect(content().string(containsString("GatewayBSdk.pagar")));
    }

    @Test
    void pedidoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/pedidos/NAO-EXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(containsString("NAO-EXISTE")));
    }
}
