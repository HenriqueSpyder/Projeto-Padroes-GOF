package br.edu.unirv.esw430.checkout.repository;

import br.edu.unirv.esw430.checkout.model.Cliente;
import br.edu.unirv.esw430.checkout.model.ItemPedido;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.StatusPedido;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PedidoRepositoryMemoria implements PedidoRepository {

    private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();

    public PedidoRepositoryMemoria() {
        recarregarDadosIniciais();
    }

    @Override
    public Optional<Pedido> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(pedidos.get(id.trim()));
    }

    @Override
    public List<Pedido> listarTodos() {
        return pedidos.values().stream()
                .sorted(Comparator.comparing(Pedido::getId))
                .toList();
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        if (pedido == null || pedido.getId() == null || pedido.getId().isBlank()) {
            throw new IllegalArgumentException("Pedido inválido para persistência.");
        }
        pedidos.put(pedido.getId(), pedido);
        return pedido;
    }

    @Override
    public synchronized void recarregarDadosIniciais() {
        pedidos.clear();
        for (Pedido pedido : massaDemonstracao()) {
            pedidos.put(pedido.getId(), pedido);
        }
    }

    private List<Pedido> massaDemonstracao() {
        return List.of(
                pedido("PED-1001",
                        new Cliente("Ana Lima", "ana.lima@email.com", "12345678901"),
                        "75901-010",
                        StatusPedido.ABERTO,
                        List.of(
                                new ItemPedido("MOU-01", "Mouse sem fio", 2, new BigDecimal("80.00")),
                                new ItemPedido("TEC-01", "Teclado mecânico", 1, new BigDecimal("90.00")))),
                pedido("PED-1002",
                        new Cliente("João Souza", "joao.souza@email.com", "98765432100"),
                        "01310-100",
                        StatusPedido.ABERTO,
                        List.of(new ItemPedido("LIV-01", "Livro de arquitetura de software", 1, new BigDecimal("89.90")))),
                pedido("PED-1003",
                        new Cliente("Maria Alves", "maria.alves@email.com", "11122233344"),
                        "75901-220",
                        StatusPedido.ABERTO,
                        List.of(new ItemPedido("MON-01", "Monitor 27 polegadas", 1, new BigDecimal("1500.00")))),
                pedido("PED-1004",
                        new Cliente("Carlos Dias", "carlos.dias@email.com", "55566677788"),
                        "75901-010",
                        StatusPedido.PAGO,
                        List.of(new ItemPedido("CAB-01", "Cabo HDMI", 1, new BigDecimal("39.90")))),
                pedido("PED-1005",
                        new Cliente("Helena Prado", "helena.prado@email.com", "99988877766"),
                        "74045-000",
                        StatusPedido.ABERTO,
                        List.of(new ItemPedido("CAD-01", "Cadeira de escritório", 1, new BigDecimal("350.00"))))
        );
    }

    private Pedido pedido(String id, Cliente cliente, String cep, StatusPedido status, List<ItemPedido> itens) {
        return new Pedido(id, cliente, cep, status, itens);
    }
}
