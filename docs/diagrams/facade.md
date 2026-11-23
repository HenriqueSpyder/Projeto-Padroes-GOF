# Diagrama de classes — Facade

Padrão estrutural **Facade** na classe `CheckoutFacade`.

O método `finalizarPedido` é a operação de alto nível. Ele consulta e valida o pedido pelo `PedidoRepository`, obtém o valor pela cadeia de `CalculadoraPedido` (via `CalculoPedidoService`), autoriza a cobrança por um `ProcessadorPagamento` e emite o `ResultadoCheckout` com o comprovante quando o pagamento é aprovado.

Os subsistemas Adapter e Decorator estão detalhados em [adapter.md](adapter.md) e [decorator.md](decorator.md).

```mermaid
classDiagram
    class CheckoutMvcController {
        +index() String
        +checkout(form) String
        +calculo(id, cupom) String
    }
    class CheckoutApiController {
        +checkout(form) ResultadoCheckout
        +calcular(id, cupom) ResultadoCalculo
    }
    class DemonstracaoCheckoutRunner {
        +run(args) void
    }
    class CheckoutFacade {
        -pedidoRepository PedidoRepository
        -calculoPedidoService CalculoPedidoService
        -processadores List
        +finalizarPedido(solicitacao) ResultadoCheckout
    }
    class PedidoRepository {
        <<interface>>
        +buscarPorId(id) Optional
        +listarTodos() List
        +salvar(pedido) Pedido
        +recarregarDadosIniciais() void
    }
    class PedidoRepositoryMemoria
    class CalculoPedidoService {
        +calcular(pedido, codigoCupom) ResultadoCalculo
        +obterCalculadora(codigoCupom) CalculadoraPedido
    }
    class ResolvedorCupom {
        +resolver(codigo) Cupom
    }
    class CupomRepository {
        <<interface>>
        +buscarPorCodigo(codigo) Optional
        +listarTodos() List
    }
    class MontadorCalculadoraPedido {
        <<interface>>
        +montar(cupom) CalculadoraPedido
    }
    class CalculadoraPedido {
        <<interface>>
        +calcular(pedido) ResultadoCalculo
    }
    class ProcessadorPagamento {
        <<interface>>
        +autorizarTransacao(requisicao) ResultadoAutorizacao
        +getNomeGateway() String
    }
    class SolicitacaoCheckout
    class ResultadoCheckout
    class Pedido

    CheckoutMvcController --> CheckoutFacade
    CheckoutApiController --> CheckoutFacade
    DemonstracaoCheckoutRunner --> CheckoutFacade
    CheckoutFacade --> PedidoRepository : consulta e salva
    CheckoutFacade --> CalculoPedidoService : calcula
    CheckoutFacade --> ProcessadorPagamento : autoriza
    CheckoutFacade ..> SolicitacaoCheckout
    CheckoutFacade ..> ResultadoCheckout
    PedidoRepository <|.. PedidoRepositoryMemoria
    PedidoRepository ..> Pedido
    CalculoPedidoService --> MontadorCalculadoraPedido
    CalculoPedidoService --> ResolvedorCupom
    CalculoPedidoService ..> CalculadoraPedido
    ResolvedorCupom --> CupomRepository
```

Versão PlantUML: [facade.puml](facade.puml).
