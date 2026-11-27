package co.edu.udea.arqsoftware.inventario.repository;

import co.edu.udea.arqsoftware.inventario.domain.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    @Query("""
            select w from Warehouse w
            where lower(w.nombre) like lower(concat('%', :term, '%'))
               or lower(w.code) like lower(concat('%', :term, '%'))
               or lower(w.ciudad) like lower(concat('%', :term, '%'))
            """)
    Page<Warehouse> search(@Param("term") String term, Pageable pageable);
}
