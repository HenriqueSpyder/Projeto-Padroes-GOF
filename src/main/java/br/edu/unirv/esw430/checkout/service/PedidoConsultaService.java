package br.edu.unirv.esw430.checkout.service;

import br.edu.unirv.esw430.checkout.exception.PedidoNaoEncontradoException;
import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.repository.CupomRepository;
import br.edu.unirv.esw430.checkout.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoConsultaService {

    private final PedidoRepository pedidoRepository;
    private final CupomRepository cupomRepository;

    public PedidoConsultaService(PedidoRepository pedidoRepository, CupomRepository cupomRepository) {
        this.pedidoRepository = pedidoRepository;
        this.cupomRepository = cupomRepository;
    }

    public List<Pedido> listar() {
        return pedidoRepository.listarTodos();
    }

    public Pedido buscar(String id) {
        if (id == null || id.isBlank()) {
            throw new PedidoNaoEncontradoException("Pedido não encontrado.");
        }
        String pedidoId = id.trim();
        return pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + pedidoId + "."));
    }

    public List<Cupom> listarCupons() {
        return cupomRepository.listarTodos();
    }

    public List<Pedido> restaurarDadosDemonstracao() {
        pedidoRepository.recarregarDadosIniciais();
        return listar();
    }
}
