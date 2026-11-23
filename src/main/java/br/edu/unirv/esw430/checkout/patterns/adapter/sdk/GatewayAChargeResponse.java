package br.edu.unirv.esw430.checkout.patterns.adapter.sdk;

public class GatewayAChargeResponse {

    private final String transactionId;
    private final String status;
    private final String authCode;
    private final String technicalMessage;

    public GatewayAChargeResponse(String transactionId, String status, String authCode, String technicalMessage) {
        this.transactionId = transactionId;
        this.status = status;
        this.authCode = authCode;
        this.technicalMessage = technicalMessage;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getTechnicalMessage() {
        return technicalMessage;
    }
}
