package br.edu.unirv.esw430.checkout.patterns.decorator;

/**
 * Decorator abstrato. Mantém o componente envolvido e repassa a mesma interface.
 */
public abstract class CalculadoraDecorator implements CalculadoraPedido {

    private final CalculadoraPedido envolvida;

    protected CalculadoraDecorator(CalculadoraPedido envolvida) {
        if (envolvida == null) {
            throw new IllegalArgumentException("A calculadora envolvida é obrigatória.");
        }
        this.envolvida = envolvida;
    }

    public CalculadoraPedido getComponente() {
        return envolvida;
    }
}
