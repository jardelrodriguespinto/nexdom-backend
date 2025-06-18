package br.com.product.nextdomtest.repository;

import br.com.product.nextdomtest.model.MovimentoEstoque;
import br.com.product.nextdomtest.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long>
{
    List<MovimentoEstoque> findByProduto(Produto produto);

    boolean existsByProdutoId(Long produtoId);

    @Query("SELECT COUNT(m) FROM MovimentoEstoque m WHERE m.produto.id = :produtoId")
    long countByProdutoId(@Param("produtoId") Long produtoId);
}