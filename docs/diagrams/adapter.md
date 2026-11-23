# Diagrama de classes — Adapter

Padrão estrutural **Adapter** no pacote `br.edu.unirv.esw430.checkout.patterns.adapter`.

A interface alvo é `ProcessadorPagamento`. `AdapterGatewayA` e `AdapterGatewayB` convertem a `RequisicaoPagamento` do checkout para as assinaturas incompatíveis de `GatewayASdk` (centavos e status textual) e `GatewayBSdk` (double e código numérico) e devolvem um `ResultadoAutorizacao`. O `CheckoutFacade` depende só da interface alvo.

```mermaid
classDiagram
    class ProcessadorPagamento {
        <<interface>>
        +GATEWAY_A String
        +GATEWAY_B String
        +autorizarTransacao(requisicao) ResultadoAutorizacao
        +getNomeGateway() String
    }
    class AdapterGatewayA {
        -sdk GatewayASdk
        -merchantId String
        +autorizarTransacao(requisicao) ResultadoAutorizacao
        +getNomeGateway() String
    }
    class AdapterGatewayB {
        -sdk GatewayBSdk
        -contaLoja String
        +autorizarTransacao(requisicao) ResultadoAutorizacao
        +getNomeGateway() String
    }
    class GatewayASdk {
        +cobrar(merchantId, valorCentavos, cardToken, orderRef) GatewayAChargeResponse
    }
    class GatewayAChargeResponse {
        -transactionId String
        -status String
        -authCode String
        -technicalMessage String
    }
    class GatewayASdkException
    class GatewayBSdk {
        +pagar(contaLoja, valorReais, documentoCliente, referenciaPedido, meioPagamento) GatewayBRetorno
    }
    class GatewayBRetorno {
        -codigoRetorno int
        -nsu String
        -mensagemOperadora String
    }
    class GatewayBSdkException
    class RequisicaoPagamento {
        -referenciaPedido String
        -valor BigDecimal
        -moeda String
        -tokenCartao String
        -documentoCliente String
        -descricao String
    }
    class ResultadoAutorizacao {
        -autorizado boolean
        -gateway String
        -idTransacao String
        -codigoAutorizacao String
        -mensagem String
        -detalheAdaptacao String
    }
    class CheckoutFacade

    ProcessadorPagamento <|.. AdapterGatewayA
    ProcessadorPagamento <|.. AdapterGatewayB
    AdapterGatewayA --> GatewayASdk : adapta
    AdapterGatewayB --> GatewayBSdk : adapta
    GatewayASdk --> GatewayAChargeResponse : cria
    GatewayASdk --> GatewayASdkException : lança
    GatewayBSdk --> GatewayBRetorno : cria
    GatewayBSdk --> GatewayBSdkException : lança
    AdapterGatewayA ..> RequisicaoPagamento : converte
    AdapterGatewayB ..> RequisicaoPagamento : converte
    AdapterGatewayA ..> ResultadoAutorizacao : produz
    AdapterGatewayB ..> ResultadoAutorizacao : produz
    CheckoutFacade --> ProcessadorPagamento : usa
```

Versão PlantUML: [adapter.puml](adapter.puml).
