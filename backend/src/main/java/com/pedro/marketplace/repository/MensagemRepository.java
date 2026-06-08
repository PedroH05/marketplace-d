package com.pedro.marketplace.repository;

import com.pedro.marketplace.entity.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByChatIdOrderByDataEnvioAsc(Long chatId);

    Optional<Mensagem> findTopByChatIdOrderByDataEnvioDesc(Long chatId);
}
