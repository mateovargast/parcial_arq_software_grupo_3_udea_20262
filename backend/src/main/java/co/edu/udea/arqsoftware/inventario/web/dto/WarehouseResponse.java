package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.domain.Warehouse;
import org.springframework.hateoas.server.core.Relation;

@Relation(itemRelation = "warehouse", collectionRelation = "warehouses")
public record WarehouseResponse(
        Long id,
        String codigoSede,
        String nombre,
        String ciudad,
        String direccion,
        String telefono,
        boolean activo) {
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getNombre(),
                warehouse.getCiudad(),
                warehouse.getDireccion(),
                warehouse.getTelefono(),
                warehouse.isActivo());
    }
}
