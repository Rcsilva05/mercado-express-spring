package br.com.mercadoexpress.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "Escreva uma pergunta antes de enviar.")
        @Size(max = 1000, message = "A pergunta deve ter no máximo 1000 caracteres.")
        String mensagem
) {
}
