package br.com.mercadoexpress.web;

import java.time.LocalDateTime;

public record ChatResponse(
        Long id,
        String pergunta,
        String resposta,
        String provedor,
        String modelo,
        LocalDateTime dataHora
) {
}
