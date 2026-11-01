package br.com.mercadoexpress.controller;

import br.com.mercadoexpress.service.ChatProviderException;
import br.com.mercadoexpress.service.ChatService;
import br.com.mercadoexpress.web.ChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void returnsTheChatResponseAsJson() throws Exception {
        given(chatService.ask("O que combina com mel?")).willReturn(
                new ChatResponse(
                        4L,
                        "O que combina com mel?",
                        "Granola artesanal.",
                        "ollama",
                        "llama3.2:1b",
                        LocalDateTime.of(2026, 11, 1, 15, 30)
                )
        );

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mensagem\":\"O que combina com mel?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.pergunta").value("O que combina com mel?"))
                .andExpect(jsonPath("$.resposta").value("Granola artesanal."))
                .andExpect(jsonPath("$.provedor").value("ollama"))
                .andExpect(jsonPath("$.modelo").value("llama3.2:1b"))
                .andExpect(jsonPath("$.dataHora").exists());
    }

    @Test
    void rejectsBlankQuestionsBeforeCallingTheService() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mensagem\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Escreva uma pergunta antes de enviar."));

        verifyNoInteractions(chatService);
    }

    @Test
    void rejectsQuestionsLongerThanOneThousandCharacters() throws Exception {
        String question = "a".repeat(1001);
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mensagem\":\"" + question + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A pergunta deve ter no máximo 1000 caracteres."));

        verifyNoInteractions(chatService);
    }

    @Test
    void returnsTheSavedConversationHistory() throws Exception {
        given(chatService.getHistory()).willReturn(List.of(
                new ChatResponse(
                        4L,
                        "O que combina com mel?",
                        "Granola artesanal.",
                        "ollama",
                        "llama3.2:1b",
                        LocalDateTime.of(2026, 11, 1, 15, 30)
                )
        ));

        mockMvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pergunta").value("O que combina com mel?"))
                .andExpect(jsonPath("$[0].resposta").value("Granola artesanal."));
    }

    @Test
    void returnsASanitizedUnavailableResponseWhenTheProviderFails() throws Exception {
        given(chatService.ask(anyString())).willThrow(
                new ChatProviderException(new IllegalStateException("private provider details"))
        );

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mensagem\":\"Pergunta válida\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("Ollama")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("private provider details")
                )));
    }
}
