package com.pedro.marketplace.repository;

import com.pedro.marketplace.entity.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByChatIdOrderByDataEnvioAsc(Long chatId);

    Optional<Mensagem> findTopByChatIdOrderByDataEnvioDesc(Long chatId);

    @Modifying
    @Query("""
            delete from Mensagem m
            where m.chat.id in :chatIds
            """)
    void deleteByChatIds(@Param("chatIds") List<Long> chatIds);
}
