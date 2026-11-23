package br.edu.unirv.esw430.checkout.repository;

import br.edu.unirv.esw430.checkout.model.Cupom;

import java.util.List;
import java.util.Optional;

public interface CupomRepository {

    Optional<Cupom> buscarPorCodigo(String codigo);

    List<Cupom> listarTodos();
}
