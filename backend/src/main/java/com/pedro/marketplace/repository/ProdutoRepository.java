package com.pedro.marketplace.repository;

import com.pedro.marketplace.entity.Produto;
import com.pedro.marketplace.entity.StatusProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByStatus(StatusProduto status);

    List<Produto> findByVendedorId(Long vendedorId);
}
