package co.edu.udea.arqsoftware.inventario.repository;

import co.edu.udea.arqsoftware.inventario.domain.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlmacenRepository extends JpaRepository<Almacen, Long> {

    boolean existsByCodigoSede(String codigoSede);
}
