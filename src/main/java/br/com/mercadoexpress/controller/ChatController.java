package br.com.mercadoexpress.controller;

import br.com.mercadoexpress.service.ChatService;
import br.com.mercadoexpress.service.ChatProviderException;
import br.com.mercadoexpress.web.ChatError;
import br.com.mercadoexpress.web.ChatRequest;
import br.com.mercadoexpress.web.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.ask(request.mensagem().trim());
    }

    @GetMapping("/historico")
    public List<ChatResponse> history() {
        return chatService.getHistory();
    }

    @ExceptionHandler(ChatProviderException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ChatError providerUnavailable(ChatProviderException exception) {
        return new ChatError(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ChatError invalidJson() {
        return new ChatError("Envie um JSON válido com o campo mensagem.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ChatError invalidRequest(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("A pergunta não pode ficar vazia e deve ter no máximo 1000 caracteres.");
        return new ChatError(message);
    }
}
