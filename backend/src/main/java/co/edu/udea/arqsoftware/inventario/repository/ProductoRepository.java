package co.edu.udea.arqsoftware.inventario.repository;

import co.edu.udea.arqsoftware.inventario.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySku(String sku);

    List<Producto> findBySkuIn(Collection<String> skus);
}
