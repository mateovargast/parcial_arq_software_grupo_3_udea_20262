package co.edu.udea.arqsoftware.inventario.repository;

import co.edu.udea.arqsoftware.inventario.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select case when count(p) > 0 then true else false end from Product p where upper(p.sku) in :skus")
    boolean existsByNormalizedSkuIn(@Param("skus") Set<String> skus);

    @Query("""
            select p from Product p
            where lower(p.sku) like lower(concat('%', :term, '%'))
               or lower(p.nombre) like lower(concat('%', :term, '%'))
               or lower(p.categoria) like lower(concat('%', :term, '%'))
            """)
    Page<Product> search(@Param("term") String term, Pageable pageable);
}
