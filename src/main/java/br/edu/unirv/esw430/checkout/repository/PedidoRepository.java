package br.edu.unirv.esw430.checkout.repository;

import br.edu.unirv.esw430.checkout.model.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository {

    Optional<Pedido> buscarPorId(String id);

    List<Pedido> listarTodos();

    Pedido salvar(Pedido pedido);

    void recarregarDadosIniciais();
}
