package br.com.mercadoexpress.service;

public class ChatProviderException extends RuntimeException {
    public ChatProviderException(Throwable cause) {
        super("O assistente está temporariamente indisponível. Verifique se o Ollama está em execução e se o modelo configurado foi instalado.", cause);
    }
}
