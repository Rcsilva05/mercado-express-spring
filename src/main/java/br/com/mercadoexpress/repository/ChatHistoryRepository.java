package br.com.mercadoexpress.repository;

import br.com.mercadoexpress.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findAllByOrderByCreatedAtDescIdDesc();
}
