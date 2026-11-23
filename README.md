# Checkout — Padrões estruturais GoF (Adapter, Decorator e Facade)

Parte técnica do Trabalho N3 da disciplina **Arquitetura de Software (ESW430 — UniRV)**. O domínio é o fechamento de pedidos (checkout) e o processamento de pagamentos de um e-commerce.

## Objetivo

Implementar, em Java com Spring Boot, um checkout executável que aplica **somente** os três padrões estruturais GoF escolhidos pelo grupo:

- **Adapter**, para autorizar pagamento em gateways com SDKs incompatíveis;
- **Decorator**, para compor o valor final do pedido com frete, imposto e cupom;
- **Facade**, para fechar o pedido em uma única operação de alto nível.

A persistência fica em memória, pela interface de repositório, para o projeto subir sem instalar banco de dados.

## Arquitetura adotada

A aplicação organiza o checkout em camadas e usa o container do Spring para injeção de dependência por construtor.

| Camada | Pacote | Papel |
| --- | --- | --- |
| Model | `model` | Pedido, itens, cupom, requisição e resultados |
| Repository | `repository` | Interface + implementação em memória (`PedidoRepository`, `CupomRepository`) |
| Service | `service` | Consulta, resolução de cupom, montagem e execução do cálculo |
| Adapter | `patterns.adapter` | Interface alvo e os dois adaptadores |
| Decorator | `patterns.decorator` | Calculadora base e decoradores encadeáveis |
| Facade | `patterns.facade` | `CheckoutFacade.finalizarPedido` |
| Controller | `controller` | MVC com Thymeleaf e API REST |
| Visão | `resources/templates` e o `CommandLineRunner` | Página web e demonstração no console |
| Config | `config` | Propriedades de frete/imposto e a demonstração |

**MVC.** `Pedido` e os demais tipos de `model` são o modelo. `CheckoutMvcController` recebe o formulário e devolve as visões Thymeleaf (`index`, `calculo`, `resultado`). `CheckoutApiController` expõe o mesmo fluxo em JSON.

**Repository.** `PedidoRepository` e `CupomRepository` são as portas de persistência. `PedidoRepositoryMemoria` e `CupomRepositoryMemoria` guardam os dados em `ConcurrentHashMap`. Não há serviço externo de banco.

**Injeção de dependência.** Classes de aplicação recebem colaboradores no construtor e são registradas com `@Service`, `@Repository`, `@Component` ou `@Configuration`. O Spring injeta a lista de `ProcessadorPagamento`, então um gateway novo entra sem alterar a fachada.

**SOLID.**

- **SRP:** cada decorador trata uma regra; cada adapter só traduz um SDK; a fachada só orquestra.
- **OCP:** novo gateway é outro adapter; nova parcela do preço é outro decorador incluído no montador.
- **LSP:** `AdapterGatewayA` e `AdapterGatewayB` são substituíveis como `ProcessadorPagamento`.
- **ISP:** `ProcessadorPagamento` só expõe a autorização; `CalculadoraPedido` só expõe o cálculo.
- **DIP:** a fachada depende de `PedidoRepository`, `CalculoPedidoService` e `ProcessadorPagamento`, não dos SDKs nem das classes concretas de frete e imposto.

Repository, MVC e a injeção do Spring não entram na contagem dos três padrões GoF. Não há Bridge, Composite, Flyweight, Proxy, Singleton manual, Strategy nem Observer.

### Adapter

`ProcessadorPagamento.autorizarTransacao` é a interface alvo.

- `GatewayASdk.cobrar` recebe o valor em **centavos** e devolve `APPROVED` ou `DECLINED`.
- `GatewayBSdk.pagar` recebe o valor em **double**, o documento do cliente e devolve um **código inteiro** (`0` aprovado, `51` recusado).
- `AdapterGatewayA` e `AdapterGatewayB` convertem a `RequisicaoPagamento` e o retorno de cada SDK para `ResultadoAutorizacao`. Exceções dos SDKs também viram resposta negada do domínio. O campo `detalheAdaptacao` mostra a chamada traduzida.

Um token que contém `RECUS` é recusado pelos dois SDKs. O Gateway B também recusa valores acima de R$ 5.000,00.

### Decorator

`CalculadoraPedido` é o componente. A cadeia, de dentro para fora, é:

`CalculoPedidoBase` → `CupomDescontoDecorator` (quando há cupom) → `FreteDecorator` → `ImpostoDecorator`

- **Base:** soma quantidade × preço dos itens.
- **Cupom:** percentual ou valor fixo sobre o subtotal, sem ultrapassar o subtotal. Cupons de demonstração: `PROMO10` (10%), `VALE20` (R$ 20,00) e `BEMVINDO` (5%).
- **Frete:** grátis quando o subtotal dos itens é de pelo menos R$ 300,00; R$ 15,00 para CEP iniciado por `74` ou `75`; R$ 29,90 nos demais.
- **Imposto:** 8% sobre (subtotal − desconto). O frete não entra na base.

### Facade

`CheckoutFacade.finalizarPedido` faz, nesta ordem:

1. busca o pedido no repositório e exige status aberto, itens e token;
2. calcula o total pela cadeia de decorators;
3. escolhe o `ProcessadorPagamento` (`GATEWAY_A` ou `GATEWAY_B`) e autoriza a transação;
4. grava o pedido como `PAGO` ou `RECUSADO`;
5. emite o comprovante somente quando o pagamento é autorizado.

### Diagramas de classes

Um diagrama por padrão, em Mermaid e PlantUML, em [`docs/diagrams`](docs/diagrams):

- [Adapter](docs/diagrams/adapter.md) e [adapter.puml](docs/diagrams/adapter.puml)
- [Decorator](docs/diagrams/decorator.md) e [decorator.puml](docs/diagrams/decorator.puml)
- [Facade](docs/diagrams/facade.md) e [facade.puml](docs/diagrams/facade.puml)

## Passos para executar

Pré-requisitos, a partir de uma máquina sem o projeto rodando:

1. Instalar um **JDK 17 ou superior** (o projeto foi exercitado com JDK 24). A pasta do JDK é a que contém `bin\java.exe` e `bin\javac.exe`.
2. Deixar `java` e `javac` no `PATH`. O `mvnw.cmd` também usa `JAVA_HOME` quando essa variável existe.
3. Ter acesso à internet na primeira execução. O Maven Wrapper baixa o Maven 3.9.9 e as dependências do Spring Boot. Não é preciso instalar Maven nem banco de dados.

Abra um terminal **novo** e confira:

```text
java -version
javac -version
echo %JAVA_HOME%
```

No PowerShell, o último comando é `echo $env:JAVA_HOME`.

Se `java` não for reconhecido, no PowerShell (troque o caminho pelo JDK desta máquina e abra outro terminal depois):

```powershell
$jdk = "C:\caminho\do\jdk"
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk, "User")
$bin = Join-Path $jdk "bin"
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
$parts = @()
if ($userPath) { $parts = $userPath -split ';' | Where-Object { $_ -and ($_ -ine $bin) } }
[Environment]::SetEnvironmentVariable("Path", ((@($bin) + $parts) -join ';'), "User")
```

Nesta máquina o JDK usado é `C:\Users\demol\.jdks\openjdk-24.0.1`. `JAVA_HOME` e `%JAVA_HOME%\bin` já foram gravados nas variáveis de ambiente do usuário do Windows. Um terminal aberto antes dessa configuração não enxerga a mudança: feche e abra o terminal de novo.

Na pasta deste projeto (a pasta que contém `pom.xml`). No Windows, `chcp 65001` deixa o console em UTF-8 para os acentos da demonstração:

```powershell
chcp 65001
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

No Prompt de Comando, os mesmos comandos são `mvnw.cmd test` e `mvnw.cmd spring-boot:run`. No Linux ou no macOS:

```bash
./mvnw test
./mvnw spring-boot:run
```

Se o Maven 3.9+ já estiver instalado e no `PATH`, `mvn test` e `mvn spring-boot:run` equivalem aos comandos do wrapper.

O que deve acontecer:

1. `test` termina com `BUILD SUCCESS` (25 testes).
2. `spring-boot:run` sobe o Tomcat na porta **8080** e o `DemonstracaoCheckoutRunner` imprime, no console, os cenários de Adapter, Decorator e Facade.
3. A aplicação permanece no ar. Abra [http://localhost:8080/](http://localhost:8080/).
4. Para encerrar, use `Ctrl+C` no terminal.

Também é possível gerar o jar e executá-lo:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\checkout-padroes-gof-1.0.0.jar
```

Se a porta 8080 estiver ocupada, altere `server.port` em `src/main/resources/application.properties` e use a nova porta na URL.

### Massa de demonstração

| Pedido | Subtotal | CEP | Uso na subida |
| --- | --- | --- | --- |
| PED-1001 | R$ 250,00 | 75901-010 | Facade paga no Gateway A com `PROMO10`. Total **R$ 258,00** |
| PED-1002 | R$ 89,90 | 01310-100 | Facade paga no Gateway B sem cupom. Total **R$ 126,99** |
| PED-1003 | R$ 1.500,00 | 75901-220 | Facade recusa no Gateway A com `VALE20`. Total **R$ 1.598,40** |
| PED-1004 | R$ 39,90 | 75901-010 | Já nasce pago; o Facade recusa novo checkout |
| PED-1005 | R$ 350,00 | 74045-000 | Permanece **aberto** para teste manual |

No formulário, use o pedido `PED-1005`, o gateway desejado, o cupom `PROMO10` (total **R$ 340,20**) ou cupom em branco (total **R$ 378,00**) e o token `tok_visa_4242`. O token `tok_recusado` nega o pagamento. O botão **Restaurar pedidos de demonstração** volta a massa ao estado inicial sem reiniciar o processo.

### API

```powershell
Invoke-RestMethod http://localhost:8080/api/pedidos
Invoke-RestMethod "http://localhost:8080/api/pedidos/PED-1005/calculo?cupom=PROMO10"
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/checkout -ContentType "application/json" -Body '{"pedidoId":"PED-1005","gateway":"GATEWAY_A","codigoCupom":"PROMO10","tokenCartao":"tok_visa_4242"}'
```

Com `curl`:

```text
curl http://localhost:8080/api/pedidos
curl "http://localhost:8080/api/pedidos/PED-1005/calculo?cupom=PROMO10"
curl -X POST http://localhost:8080/api/checkout -H "Content-Type: application/json" -d "{\"pedidoId\":\"PED-1005\",\"gateway\":\"GATEWAY_A\",\"codigoCupom\":\"PROMO10\",\"tokenCartao\":\"tok_visa_4242\"}"
```

`POST /api/pedidos/restaurar` recarrega os pedidos de demonstração.

## Integrantes

- Henrique Antunes da Silva
- Guilherme Nogueira Sousa
- João Paulo Gomes de Oliveira Sousa
- Yasmim Tizo Ferreira

## Ferramentas de IA utilizadas

O projeto foi elaborado no **Cursor**. O agente de código do Cursor foi usado para estruturar a aplicação Spring Boot, implementar Adapter, Decorator e Facade no domínio de checkout, escrever os testes, gerar os diagramas de classes em Mermaid e PlantUML e redigir este README. Os cenários e os testes foram executados em seguida com o Maven (`mvn test` / `mvnw test`) para conferir a compilação e os valores do checkout.
