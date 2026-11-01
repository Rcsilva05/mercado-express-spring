package br.com.mercadoexpress.service;

import br.com.mercadoexpress.model.ChatHistory;
import br.com.mercadoexpress.model.Product;
import br.com.mercadoexpress.repository.ChatHistoryRepository;
import br.com.mercadoexpress.repository.ProductRepository;
import br.com.mercadoexpress.web.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String SYSTEM_PROMPT = """
            Você é o assistente virtual do Mercado Express, um mercado de bairro brasileiro.
            Responda sempre em português do Brasil, com simpatia e clareza. Ajude clientes a
            escolher produtos, comparar opções e entender ingredientes com base no catálogo
            recebido. Não invente produtos, preços, estoque, ingredientes ou políticas da loja.
            Não recomende como disponível um produto cujo estoque seja zero.
            Ao responder sobre um produto, informe o nome, o preço exato e as unidades em
            estoque mostradas no catálogo. Só diga que está disponível se o estoque informado
            for maior que zero; nunca diga que vai verificar o estoque, pois os números já
            representam os dados atuais do catálogo. Se não encontrar o produto, diga isso
            sem inventar uma confirmação.
            Se a informação não estiver no catálogo, diga isso claramente e sugira falar com a
            equipe. Não forneça diagnósticos médicos nem alegações de saúde. Seja conciso.
            """;

    private final ChatClient chatClient;
    private final ProductRepository productRepository;
    private final ChatHistoryRepository historyRepository;
    private final String model;

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            ProductRepository productRepository,
            ChatHistoryRepository historyRepository,
            @Value("${spring.ai.ollama.chat.options.model}") String model
    ) {
        this.chatClient = chatClientBuilder.build();
        this.productRepository = productRepository;
        this.historyRepository = historyRepository;
        this.model = model;
    }

    public ChatResponse ask(String message) {
        String prompt = buildUserPrompt(message);
        String answer;
        try {
            answer = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(prompt)
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            log.warn("A chamada ao Ollama falhou ({})", exception.getClass().getSimpleName());
            throw new ChatProviderException(exception);
        }

        if (answer == null || answer.isBlank()) {
            throw new ChatProviderException(new IllegalStateException("O provedor retornou uma resposta vazia."));
        }

        ChatHistory history = new ChatHistory();
        history.setQuestion(message);
        history.setAnswer(answer.trim());
        history.setProvider("ollama");
        history.setModel(model);
        ChatHistory saved = historyRepository.save(history);
        return toResponse(saved);
    }

    public List<ChatResponse> getHistory() {
        return historyRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private String buildUserPrompt(String message) {
        List<Product> products = productRepository.findByActiveTrueOrderByNameAsc();
        String catalog = products.isEmpty()
                ? "O catálogo está vazio no momento."
                : products.stream().map(this::formatProduct).collect(Collectors.joining("\n"));

        return """
                Catálogo atual do Mercado Express:
                %s

                Pergunta do cliente:
                %s
                """.formatted(catalog, message);
    }

    private String formatProduct(Product product) {
        BigDecimal price = product.getPrice();
        return "- Produto: %s | categoria: %s | preço exato: R$ %s | unidades em estoque: %d | detalhes: %s"
                .formatted(
                        product.getName(),
                        product.getCategory(),
                        price == null ? "não informado" : price.toPlainString(),
                        product.getStock(),
                        product.getDescription()
                );
    }

    private ChatResponse toResponse(ChatHistory history) {
        return new ChatResponse(
                history.getId(),
                history.getQuestion(),
                history.getAnswer(),
                history.getProvider().toLowerCase(Locale.ROOT),
                history.getModel(),
                history.getCreatedAt()
        );
    }
}
