# Diagrama de classes — Adapter

Padrão estrutural **Adapter** no pacote `br.edu.unirv.esw430.checkout.patterns.adapter`.

A interface alvo é `ProcessadorPagamento`. `ProcessadorGatewayA` e `ProcessadorGatewayB` convertem a `RequisicaoPagamento` do checkout para as assinaturas incompatíveis de `GatewayASdk` (centavos e status textual) e `GatewayBSdk` (double e código numérico) e devolvem um `ResultadoAutorizacao`. A validação da requisição, o mascaramento do token e o resultado negado ficam em `ProcessadorGatewayBase`. O `CheckoutFacade` depende só da interface alvo.

```mermaid
classDiagram
    class ProcessadorPagamento {
        <<interface>>
        +autorizarTransacao(requisicao) ResultadoAutorizacao
        +getNomeGateway() String
    }
    class ProcessadorGatewayBase {
        <<abstract>>
        #validar(requisicao) void
        #negado(mensagem, detalhe) ResultadoAutorizacao
        #mascarar(token) String
    }
    class ProcessadorGatewayA {
        +NOME String$
        -sdk GatewayASdk
        -merchantId String
        +autorizarTransacao(requisicao) ResultadoAutorizacao
        +getNomeGateway() String
    }
    class ProcessadorGatewayB {
        +NOME String$
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

    ProcessadorPagamento <|.. ProcessadorGatewayBase
    ProcessadorGatewayBase <|-- ProcessadorGatewayA
    ProcessadorGatewayBase <|-- ProcessadorGatewayB
    ProcessadorGatewayA --> GatewayASdk : adapta
    ProcessadorGatewayB --> GatewayBSdk : adapta
    GatewayASdk --> GatewayAChargeResponse : cria
    GatewayASdk --> GatewayASdkException : lança
    GatewayBSdk --> GatewayBRetorno : cria
    GatewayBSdk --> GatewayBSdkException : lança
    ProcessadorGatewayA ..> RequisicaoPagamento : converte
    ProcessadorGatewayB ..> RequisicaoPagamento : converte
    ProcessadorGatewayA ..> ResultadoAutorizacao : produz
    ProcessadorGatewayB ..> ResultadoAutorizacao : produz
    CheckoutFacade --> ProcessadorPagamento : usa
```

Versão PlantUML: [adapter.puml](adapter.puml).
