package br.com.product.nextdomtest.repository;

import br.com.product.nextdomtest.enums.TipoProduto;
import br.com.product.nextdomtest.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>
{
    // Buscar produtos por tipo
    List<Produto> findByTipo(TipoProduto tipo);
}
