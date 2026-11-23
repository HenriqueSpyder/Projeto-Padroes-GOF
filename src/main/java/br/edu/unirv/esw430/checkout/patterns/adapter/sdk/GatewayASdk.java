package br.edu.unirv.esw430.checkout.patterns.adapter.sdk;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * SDK simulado e incompatível com o domínio. Cobra em centavos e devolve status textual em inglês.
 */
@Component
public class GatewayASdk {

    public GatewayAChargeResponse cobrar(String merchantId, long valorCentavos, String cardToken, String orderRef) {
        if (merchantId == null || merchantId.isBlank()) {
            throw new GatewayASdkException("merchantId is required");
        }
        if (cardToken == null || cardToken.isBlank()) {
            throw new GatewayASdkException("cardToken is required");
        }
        if (orderRef == null || orderRef.isBlank()) {
            throw new GatewayASdkException("orderRef is required");
        }
        if (valorCentavos <= 0) {
            throw new GatewayASdkException("amount must be positive");
        }
        boolean approved = !cardToken.toUpperCase(Locale.ROOT).contains("RECUS");
        String status = approved ? "APPROVED" : "DECLINED";
        String authCode = approved ? "A" + valorCentavos : "";
        String technicalMessage = approved ? "OK" : "DO_NOT_HONOR";
        return new GatewayAChargeResponse(
                "TXA-" + orderRef + "-" + valorCentavos,
                status,
                authCode,
                technicalMessage);
    }
}
