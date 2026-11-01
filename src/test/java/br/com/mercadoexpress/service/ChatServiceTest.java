package br.com.mercadoexpress.service;

import br.com.mercadoexpress.model.ChatHistory;
import br.com.mercadoexpress.model.Product;
import br.com.mercadoexpress.repository.ChatHistoryRepository;
import br.com.mercadoexpress.repository.ProductRepository;
import br.com.mercadoexpress.web.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatClient.Builder clientBuilder;
    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec responseSpec;
    private ProductRepository productRepository;
    private ChatHistoryRepository historyRepository;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        clientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);
        productRepository = mock(ProductRepository.class);
        historyRepository = mock(ChatHistoryRepository.class);
        given(clientBuilder.build()).willReturn(chatClient);
        given(chatClient.prompt()).willReturn(requestSpec);
        given(requestSpec.system(anyString())).willReturn(requestSpec);
        given(requestSpec.user(anyString())).willReturn(requestSpec);
        given(requestSpec.call()).willReturn(responseSpec);
        chatService = new ChatService(clientBuilder, productRepository, historyRepository, "llama3.2:1b");
    }

    @Test
    void asksOllamaWithCatalogAndPersistsTheAnswer() {
        Product product = new Product();
        product.setName("Morango orgânico");
        product.setCategory("Hortifruti");
        product.setPrice(new BigDecimal("18.90"));
        product.setStock(24);
        product.setDescription("Direto do produtor.");
        given(productRepository.findByActiveTrueOrderByNameAsc()).willReturn(List.of(product));
        given(responseSpec.content()).willReturn("O morango está no catálogo.");
        given(historyRepository.save(any(ChatHistory.class))).willAnswer(invocation -> {
            ChatHistory history = invocation.getArgument(0);
            history.setId(17L);
            history.setCreatedAt(LocalDateTime.of(2026, 11, 1, 15, 30));
            return history;
        });

        ChatResponse response = chatService.ask("Tem morango?");

        assertEquals(17L, response.id());
        assertEquals("Tem morango?", response.pergunta());
        assertEquals("O morango está no catálogo.", response.resposta());
        assertEquals("ollama", response.provedor());
        assertEquals("llama3.2:1b", response.modelo());
        assertNotNull(response.dataHora());

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(prompt.capture());
        assertTrue(prompt.getValue().contains("Morango orgânico"));
        assertTrue(prompt.getValue().contains("preço exato: R$ 18.90"));
        assertTrue(prompt.getValue().contains("unidades em estoque: 24"));
        assertTrue(prompt.getValue().contains("Tem morango?"));
        ArgumentCaptor<ChatHistory> history = ArgumentCaptor.forClass(ChatHistory.class);
        verify(historyRepository).save(history.capture());
        assertEquals("Tem morango?", history.getValue().getQuestion());
    }

    @Test
    void reportsProviderFailureWithoutSavingHistory() {
        given(productRepository.findByActiveTrueOrderByNameAsc()).willReturn(List.of());
        given(responseSpec.content()).willThrow(new IllegalStateException("private provider response"));

        ChatProviderException exception = assertThrows(
                ChatProviderException.class,
                () -> chatService.ask("Que produto você recomenda?")
        );

        assertFalse(exception.getMessage().contains("private provider response"));
        verify(historyRepository, never()).save(any());
    }
}
