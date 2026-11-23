package br.edu.unirv.esw430.checkout.service;

import br.edu.unirv.esw430.checkout.exception.CheckoutInvalidoException;
import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.repository.CupomRepository;
import org.springframework.stereotype.Service;

@Service
public class ResolvedorCupom {

    private final CupomRepository cupomRepository;

    public ResolvedorCupom(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    public Cupom resolver(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        String normalizado = codigo.trim();
        return cupomRepository.buscarPorCodigo(normalizado)
                .orElseThrow(() -> new CheckoutInvalidoException("Cupom inválido: " + normalizado + "."));
    }
}
