package br.edu.unirv.esw430.checkout.service;

import br.edu.unirv.esw430.checkout.model.Cupom;
import br.edu.unirv.esw430.checkout.model.Pedido;
import br.edu.unirv.esw430.checkout.model.ResultadoCalculo;
import br.edu.unirv.esw430.checkout.patterns.decorator.CalculadoraDecorator;
import br.edu.unirv.esw430.checkout.patterns.decorator.CalculadoraPedido;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class CalculoPedidoService {

    private final ResolvedorCupom resolvedorCupom;
    private final MontadorCalculadoraPedido montador;

    public CalculoPedidoService(ResolvedorCupom resolvedorCupom, MontadorCalculadoraPedido montador) {
        this.resolvedorCupom = resolvedorCupom;
        this.montador = montador;
    }

    public ResultadoCalculo calcular(Pedido pedido, String codigoCupom) {
        return obterCalculadora(codigoCupom).calcular(pedido);
    }

    public CalculadoraPedido obterCalculadora(String codigoCupom) {
        Cupom cupom = resolvedorCupom.resolver(codigoCupom);
        return montador.montar(cupom);
    }

    public String descreverCadeia(String codigoCupom) {
        return descrever(obterCalculadora(codigoCupom));
    }

    public String descrever(CalculadoraPedido calculadora) {
        if (calculadora == null) {
            return "";
        }
        Deque<String> nomes = new ArrayDeque<>();
        CalculadoraPedido atual = calculadora;
        while (atual instanceof CalculadoraDecorator decorator) {
            nomes.addFirst(decorator.getClass().getSimpleName());
            atual = decorator.getComponente();
        }
        nomes.addFirst(atual.getClass().getSimpleName());
        return String.join(" -> ", nomes);
    }
}
