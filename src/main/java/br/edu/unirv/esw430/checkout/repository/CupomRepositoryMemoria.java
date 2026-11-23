package br.edu.unirv.esw430.checkout.repository;

import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.model.TipoCupom;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CupomRepositoryMemoria implements CupomRepository {

    private final Map<String, Cupom> cupons = new ConcurrentHashMap<>();

    public CupomRepositoryMemoria() {
        salvar(new Cupom("PROMO10", TipoCupom.PERCENTUAL, new BigDecimal("10"), "10% de desconto nos itens"));
        salvar(new Cupom("VALE20", TipoCupom.VALOR_FIXO, new BigDecimal("20.00"), "R$ 20,00 de desconto nos itens"));
        salvar(new Cupom("BEMVINDO", TipoCupom.PERCENTUAL, new BigDecimal("5"), "5% de desconto de boas-vindas"));
    }

    @Override
    public Optional<Cupom> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cupons.get(codigo.trim().toUpperCase(Locale.ROOT)));
    }

    @Override
    public List<Cupom> listarTodos() {
        return cupons.values().stream()
                .sorted(Comparator.comparing(Cupom::getCodigo))
                .toList();
    }

    private void salvar(Cupom cupom) {
        cupons.put(cupom.getCodigo(), cupom);
    }
}
