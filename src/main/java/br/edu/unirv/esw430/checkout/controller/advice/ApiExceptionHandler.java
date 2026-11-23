package br.edu.unirv.esw430.checkout.controller.advice;

import br.edu.unirv.esw430.checkout.controller.dto.ErroResposta;
import br.edu.unirv.esw430.checkout.exception.CheckoutInvalidoException;
import br.edu.unirv.esw430.checkout.exception.PedidoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PedidoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(PedidoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResposta(ex.getMessage(), 404));
    }

    @ExceptionHandler(CheckoutInvalidoException.class)
    public ResponseEntity<ErroResposta> checkoutInvalido(CheckoutInvalidoException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta(ex.getMessage(), 400));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + " " + b)
                .orElse("Dados inválidos.");
        return ResponseEntity.badRequest().body(new ErroResposta(mensagem, 400));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta(ex.getMessage(), 400));
    }
}
