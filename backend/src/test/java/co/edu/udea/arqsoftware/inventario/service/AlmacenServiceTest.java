package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.domain.Almacen;
import co.edu.udea.arqsoftware.inventario.repository.AlmacenRepository;
import co.edu.udea.arqsoftware.inventario.service.exception.ConflictoException;
import co.edu.udea.arqsoftware.inventario.service.exception.RecursoNoEncontradoException;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static co.edu.udea.arqsoftware.inventario.TestData.almacen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlmacenServiceTest {

    @Mock
    private AlmacenRepository almacenRepository;

    @InjectMocks
    private AlmacenService service;

    private final AlmacenRequest request = new AlmacenRequest("BAQ-01", "Almacén Barranquilla", "Barranquilla",
            "Calle 72 # 50-10", null);

    @Test
    void crearGuardaLaSede() {
        when(almacenRepository.existsByCodigoSede("BAQ-01")).thenReturn(false);
        when(almacenRepository.save(any(Almacen.class))).thenAnswer(inv -> {
            Almacen almacen = inv.getArgument(0);
            almacen.setId(4L);
            return almacen;
        });

        AlmacenResponse creado = service.crear(request);

        assertThat(creado.id()).isEqualTo(4L);
        assertThat(creado.codigoSede()).isEqualTo("BAQ-01");
        assertThat(creado.activo()).isTrue();
    }

    @Test
    void crearConCodigoDeSedeDuplicadoLanzaConflicto() {
        when(almacenRepository.existsByCodigoSede("BAQ-01")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("BAQ-01");
        verify(almacenRepository, never()).save(any());
    }

    @Test
    void obtenerDevuelveLaSede() {
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen(1L, "MED-01")));

        assertThat(service.obtener(1L).codigoSede()).isEqualTo("MED-01");
    }

    @Test
    void obtenerSedeInexistenteLanzaNoEncontrado() {
        when(almacenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
