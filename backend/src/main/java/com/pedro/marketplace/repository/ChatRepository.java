package com.pedro.marketplace.repository;

import com.pedro.marketplace.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByProdutoIdAndCompradorIdAndVendedorId(Long produtoId, Long compradorId, Long vendedorId);

    @Query("""
            select c.id from Chat c
            where c.produto.id = :produtoId
            """)
    List<Long> findIdsByProdutoId(@Param("produtoId") Long produtoId);

    @Modifying
    @Query("""
            delete from Chat c
            where c.produto.id = :produtoId
            """)
    void deleteByProdutoId(@Param("produtoId") Long produtoId);

    @Query("""
            select c from Chat c
            where (c.comprador.id = :usuarioId and c.ocultoParaComprador = false)
               or (c.vendedor.id = :usuarioId and c.ocultoParaVendedor = false)
            """)
    List<Chat> findByParticipanteId(@Param("usuarioId") Long usuarioId);

    @Query("""
            select count(c) > 0 from Chat c
            where c.id = :chatId
              and (c.comprador.id = :usuarioId or c.vendedor.id = :usuarioId)
            """)
    boolean existsByIdAndParticipanteId(@Param("chatId") Long chatId, @Param("usuarioId") Long usuarioId);
}
