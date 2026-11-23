# Diagrama de classes — Decorator

Padrão estrutural **Decorator** no pacote `br.edu.unirv.esw430.checkout.patterns.decorator`.

`CalculadoraPedido` é o componente. `CalculoPedidoBase` soma os itens. `CalculadoraDecorator` guarda o componente envolvido. `CupomDescontoDecorator`, `FreteDecorator` e `ImpostoDecorator` acrescentam, cada um, uma responsabilidade ao `ResultadoCalculo`.

`MontadorCalculadoraPedidoPadrao` só compõe a cadeia (subtotal, cupom quando existe, frete e imposto). Ele não é um quarto padrão GoF: a ordem fica fora dos decoradores para o imposto continuar enxergando o desconto.

```mermaid
classDiagram
    class CalculadoraPedido {
        <<interface>>
        +calcular(pedido) ResultadoCalculo
    }
    class CalculoPedidoBase {
        +calcular(pedido) ResultadoCalculo
    }
    class CalculadoraDecorator {
        -envolvida CalculadoraPedido
        +getComponente() CalculadoraPedido
    }
    class CupomDescontoDecorator {
        -cupom Cupom
        +calcular(pedido) ResultadoCalculo
    }
    class FreteDecorator {
        -valorInterno BigDecimal
        -valorNacional BigDecimal
        -limiteGratis BigDecimal
        -prefixosInternos List
        +calcular(pedido) ResultadoCalculo
    }
    class ImpostoDecorator {
        -aliquota BigDecimal
        +calcular(pedido) ResultadoCalculo
    }
    class MontadorCalculadoraPedido {
        <<interface>>
        +montar(cupom) CalculadoraPedido
    }
    class MontadorCalculadoraPedidoPadrao {
        -freteProperties FreteProperties
        -impostoProperties ImpostoProperties
        +montar(cupom) CalculadoraPedido
    }
    class CalculoPedidoService {
        +calcular(pedido, codigoCupom) ResultadoCalculo
        +obterCalculadora(codigoCupom) CalculadoraPedido
        +descrever(calculadora) String
    }
    class ResultadoCalculo {
        -subtotal BigDecimal
        -desconto BigDecimal
        -frete BigDecimal
        -imposto BigDecimal
        -total BigDecimal
        -linhas List
    }
    class LinhaCalculo {
        -descricao String
        -valor BigDecimal
    }
    class Pedido
    class Cupom

    CalculadoraPedido <|.. CalculoPedidoBase
    CalculadoraPedido <|.. CalculadoraDecorator
    CalculadoraDecorator <|-- CupomDescontoDecorator
    CalculadoraDecorator <|-- FreteDecorator
    CalculadoraDecorator <|-- ImpostoDecorator
    CalculadoraDecorator o--> CalculadoraPedido : envolvida
    CupomDescontoDecorator --> Cupom
    MontadorCalculadoraPedido <|.. MontadorCalculadoraPedidoPadrao
    MontadorCalculadoraPedidoPadrao ..> CalculoPedidoBase : cria
    MontadorCalculadoraPedidoPadrao ..> CupomDescontoDecorator : cria
    MontadorCalculadoraPedidoPadrao ..> FreteDecorator : cria
    MontadorCalculadoraPedidoPadrao ..> ImpostoDecorator : cria
    CalculoPedidoService --> MontadorCalculadoraPedido
    CalculoPedidoService ..> CalculadoraPedido : usa
    CalculadoraPedido ..> Pedido
    CalculadoraPedido ..> ResultadoCalculo
    ResultadoCalculo o--> LinhaCalculo
```

Versão PlantUML: [decorator.puml](decorator.puml).
