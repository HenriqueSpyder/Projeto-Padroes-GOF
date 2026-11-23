package br.edu.unirv.esw430.checkout.service;

import br.edu.unirv.esw430.checkout.model.Comprovante;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoAutorizacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Monta o comprovante de um pagamento autorizado.
 */
@Component
public class EmissorComprovante {

    private static final DateTimeFormatter NUMERO_COMPROVANTE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public Comprovante emitir(Pedido pedido, ResultadoAutorizacao autorizacao, BigDecimal total) {
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
}
