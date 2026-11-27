package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.domain.Almacen;
import co.edu.udea.arqsoftware.inventario.domain.Inventario;
import co.edu.udea.arqsoftware.inventario.domain.Producto;
import co.edu.udea.arqsoftware.inventario.domain.UnidadMedida;
import co.edu.udea.arqsoftware.inventario.observability.InventarioMetrics;
import co.edu.udea.arqsoftware.inventario.repository.AlmacenRepository;
import co.edu.udea.arqsoftware.inventario.repository.InventarioRepository;
import co.edu.udea.arqsoftware.inventario.repository.ProductoRepository;
import co.edu.udea.arqsoftware.inventario.repository.ResumenInventarioView;
import co.edu.udea.arqsoftware.inventario.service.exception.ConflictoException;
import co.edu.udea.arqsoftware.inventario.service.exception.RecursoNoEncontradoException;
import co.edu.udea.arqsoftware.inventario.service.exception.SolicitudInvalidaException;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioCargaRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.PageResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.ResumenInventarioResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static co.edu.udea.arqsoftware.inventario.TestData.almacen;
import static co.edu.udea.arqsoftware.inventario.TestData.inventario;
import static co.edu.udea.arqsoftware.inventario.TestData.itemRequest;
import static co.edu.udea.arqsoftware.inventario.TestData.producto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private AlmacenRepository almacenRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private InventarioRepository inventarioRepository;
    @Mock
    private InventarioMetrics metrics;

    @InjectMocks
    private InventarioService service;

    private Almacen sede;

    @BeforeEach
    void setUp() {
        sede = almacen(1L, "MED-01");
    }

    @Test
    void consultarDevuelvePaginaDeInventarioDeLaSede() {
        Pageable pageable = PageRequest.of(0, 10);
        Producto teclado = producto(5L, "TEC-001", "189900.00");
        when(almacenRepository.existsById(1L)).thenReturn(true);
        when(inventarioRepository.findByAlmacenId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(inventario(7L, sede, teclado, 40, 10)), pageable, 1));

        PageResponse<InventarioItemResponse> resultado = service.consultar(1L, pageable);

        assertThat(resultado.totalElements()).isEqualTo(1);
        assertThat(resultado.content()).singleElement().satisfies(item -> {
            assertThat(item.sku()).isEqualTo("TEC-001");
            assertThat(item.cantidad()).isEqualTo(40);
            assertThat(item.almacenId()).isEqualTo(1L);
            assertThat(item.stockBajo()).isFalse();
        });
    }

    @Test
    void consultarSedeInexistenteLanzaNoEncontrado() {
        when(almacenRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.consultar(99L, PageRequest.of(0, 10)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
        verify(inventarioRepository, never()).findByAlmacenId(any(), any());
    }

    @Test
    void resumirCalculaTotalesDeLaSede() {
        ResumenInventarioView vista = mock(ResumenInventarioView.class);
        when(vista.getTotalProductos()).thenReturn(6L);
        when(vista.getUnidadesTotales()).thenReturn(486L);
        when(vista.getValorTotal()).thenReturn(new BigDecimal("1000.00"));
        when(vista.getProductosStockBajo()).thenReturn(2L);
        when(almacenRepository.existsById(1L)).thenReturn(true);
        when(inventarioRepository.resumirPorAlmacen(1L)).thenReturn(vista);

        ResumenInventarioResponse resumen = service.resumir(1L);

        assertThat(resumen).isEqualTo(new ResumenInventarioResponse(1L, 6, 486, new BigDecimal("1000.00"), 2));
    }

    @Test
    void cargarCreaProductosNuevosYReutilizaLosExistentes() {
        Producto existente = producto(5L, "TEC-001", "189900.00");
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(inventarioRepository.findSkusExistentesEnAlmacen(eq(1L), anyCollection())).thenReturn(List.of());
        when(productoRepository.findBySkuIn(anyCollection())).thenReturn(List.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.saveAll(anyCollection())).thenAnswer(inv -> inv.getArgument(0));

        List<InventarioItemResponse> creados = service.cargar(1L,
                new InventarioCargaRequest(List.of(itemRequest("TEC-001", 30), itemRequest("NEW-001", 50))));

        assertThat(creados).extracting(InventarioItemResponse::sku).containsExactly("TEC-001", "NEW-001");
        assertThat(creados).extracting(InventarioItemResponse::cantidad).containsExactly(30, 50);

        ArgumentCaptor<Producto> productoGuardado = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository, times(1)).save(productoGuardado.capture());
        assertThat(productoGuardado.getValue().getSku()).isEqualTo("NEW-001");
        verify(metrics).registrarCarga("MED-01", 2, 1);
    }

    @Test
    void cargarUsaStockMinimoCeroCuandoNoSeEnvia() {
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(inventarioRepository.findSkusExistentesEnAlmacen(eq(1L), anyCollection())).thenReturn(List.of());
        when(productoRepository.findBySkuIn(anyCollection())).thenReturn(List.of());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.saveAll(anyCollection())).thenAnswer(inv -> inv.getArgument(0));
        var item = new InventarioItemRequest("NEW-002", "Cable", null, "Tecnología", UnidadMedida.UNIDAD,
                BigDecimal.TEN, 3, null);

        List<InventarioItemResponse> creados = service.cargar(1L, new InventarioCargaRequest(List.of(item)));

        assertThat(creados.getFirst().stockMinimo()).isZero();
    }

    @Test
    void cargarEnSedeInexistenteLanzaNoEncontrado() {
        when(almacenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cargar(99L, new InventarioCargaRequest(List.of(itemRequest("A", 1)))))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cargarConSkuRepetidoEnLaSolicitudLanzaSolicitudInvalida() {
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(sede));

        assertThatThrownBy(() -> service.cargar(1L,
                new InventarioCargaRequest(List.of(itemRequest("DUP-1", 1), itemRequest("DUP-1", 2)))))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("DUP-1");
        verify(metrics).registrarCargaRechazada("sku_repetido_en_solicitud");
        verify(inventarioRepository, never()).saveAll(anyCollection());
    }

    @Test
    void cargarConSkuYaRegistradoEnLaSedeLanzaConflicto() {
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(inventarioRepository.findSkusExistentesEnAlmacen(eq(1L), anyCollection())).thenReturn(List.of("TEC-001"));

        assertThatThrownBy(() -> service.cargar(1L, new InventarioCargaRequest(List.of(itemRequest("TEC-001", 1)))))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("TEC-001")
                .hasMessageContaining("MED-01");
        verify(metrics).registrarCargaRechazada("sku_existente_en_sede");
        verify(metrics, never()).registrarCarga(anyString(), anyInt(), anyInt());
        verify(inventarioRepository, never()).saveAll(anyCollection());
    }

    @Test
    void inventarioMarcaStockBajoCuandoCantidadEsMenorOIgualAlMinimo() {
        Producto p = producto(1L, "X", "1.00");
        Inventario igual = inventario(1L, sede, p, 10, 10);
        Inventario mayor = inventario(2L, sede, p, 11, 10);

        assertThat(igual.isStockBajo()).isTrue();
        assertThat(mayor.isStockBajo()).isFalse();
    }
}
