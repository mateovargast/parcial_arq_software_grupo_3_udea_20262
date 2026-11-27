package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.InventarioService;
import co.edu.udea.arqsoftware.inventario.service.exception.ConflictoException;
import co.edu.udea.arqsoftware.inventario.service.exception.RecursoNoEncontradoException;
import co.edu.udea.arqsoftware.inventario.web.assembler.InventarioModelAssembler;
import co.edu.udea.arqsoftware.inventario.web.dto.PageResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.ResumenInventarioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static co.edu.udea.arqsoftware.inventario.TestData.itemResponse;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventarioController.class)
@Import(InventarioModelAssembler.class)
class InventarioControllerTest {

    private static final String URL = "/api/almacenes/{sedeId}/inventario";
    private static final String VERSION = "X-API-Version";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InventarioService inventarioService;

    private void paginaConTresProductos() {
        when(inventarioService.consultar(eq(1L), any(Pageable.class))).thenReturn(new PageResponse<>(
                List.of(itemResponse(1L, 3, 10), itemResponse(2L, 0, 10), itemResponse(3L, 50, 10)), 0, 3, 7, 3));
    }

    @Test
    void consultarV1DevuelvePaginaHalConEnlaces() throws Exception {
        paginaConTresProductos();

        mvc.perform(get(URL, 1).param("page", "0").param("size", "3").header(VERSION, "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.inventario", hasSize(3)))
                .andExpect(jsonPath("$._embedded.inventario[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$._embedded.inventario[0].stockBajo").value(true))
                .andExpect(jsonPath("$._embedded.inventario[0].valorTotal").doesNotExist())
                .andExpect(jsonPath("$._embedded.inventario[0]._links.producto.href", endsWith("/api/productos/1")))
                .andExpect(jsonPath("$._embedded.inventario[0]._links.almacen.href", endsWith("/api/almacenes/1")))
                .andExpect(jsonPath("$._links.self.href", containsString("page=0")))
                .andExpect(jsonPath("$._links.next.href", containsString("page=1")))
                .andExpect(jsonPath("$._links.last.href", containsString("page=2")))
                .andExpect(jsonPath("$._links.resumen").doesNotExist())
                .andExpect(jsonPath("$.page.totalElements").value(7))
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    void consultarV2IncluyeValorTotalEstadoYEnlaceAlResumen() throws Exception {
        paginaConTresProductos();

        mvc.perform(get(URL, 1).param("size", "3").header(VERSION, "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.inventario[0].valorTotal").value(3000.00))
                .andExpect(jsonPath("$._embedded.inventario[0].estadoStock").value("BAJO"))
                .andExpect(jsonPath("$._embedded.inventario[1].estadoStock").value("AGOTADO"))
                .andExpect(jsonPath("$._embedded.inventario[2].estadoStock").value("NORMAL"))
                .andExpect(jsonPath("$._embedded.inventario[2].alerta").doesNotExist())
                .andExpect(jsonPath("$._links.resumen.href", endsWith("/api/almacenes/1/inventario/resumen")));
    }

    @Test
    void consultarSinHeaderDeVersionDevuelve400() throws Exception {
        mvc.perform(get(URL, 1))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Versión de API requerida"));
        verifyNoInteractions(inventarioService);
    }

    @Test
    void consultarConVersionNoSoportadaDevuelve400() throws Exception {
        mvc.perform(get(URL, 1).header(VERSION, "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Versión de API no soportada"));
    }

    @Test
    void consultarSedeInexistenteDevuelve404() throws Exception {
        when(inventarioService.consultar(eq(99L), any(Pageable.class)))
                .thenThrow(new RecursoNoEncontradoException("Almacén", 99L));

        mvc.perform(get(URL, 99).header(VERSION, "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail", containsString("99")));
    }

    @Test
    void consultarConTamanoDePaginaMayorAlMaximoLoLimita() throws Exception {
        when(inventarioService.consultar(eq(1L), any(Pageable.class))).thenAnswer(inv -> {
            Pageable pageable = inv.getArgument(1);
            return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
        });

        mvc.perform(get(URL, 1).param("size", "1000").header(VERSION, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void resumenSoloExisteEnV2() throws Exception {
        when(inventarioService.resumir(1L))
                .thenReturn(new ResumenInventarioResponse(1L, 6, 486, new BigDecimal("12345.00"), 2));

        mvc.perform(get(URL + "/resumen", 1).header(VERSION, "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productosStockBajo").value(2))
                .andExpect(jsonPath("$._links.self.href", endsWith("/inventario/resumen")));

        mvc.perform(get(URL + "/resumen", 1).header(VERSION, "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cargarDevuelve201ConLocationYEnlaces() throws Exception {
        when(inventarioService.cargar(eq(1L), any())).thenReturn(List.of(itemResponse(1L, 30, 5)));

        mvc.perform(post(URL, 1).header(VERSION, "1").contentType(MediaType.APPLICATION_JSON).content("""
                        {"productos":[{"sku":"SKU-1","nombre":"Teclado","categoria":"Tecnología",
                        "unidadMedida":"UNIDAD","precioUnitario":1000,"cantidadInicial":30,"stockMinimo":5}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/almacenes/1/inventario")))
                .andExpect(jsonPath("$._embedded.inventario[0].cantidad").value(30))
                .andExpect(jsonPath("$._links.inventario.href").exists())
                .andExpect(jsonPath("$._links.almacen.href", endsWith("/api/almacenes/1")));
        verify(inventarioService).cargar(eq(1L), any());
    }

    @Test
    void cargarConDatosInvalidosDevuelve400ConDetalleDeCampos() throws Exception {
        mvc.perform(post(URL, 1).header(VERSION, "1").contentType(MediaType.APPLICATION_JSON).content("""
                        {"productos":[{"sku":"","nombre":"Teclado","categoria":"Tecnología",
                        "unidadMedida":"UNIDAD","precioUnitario":-1,"cantidadInicial":30}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores", hasSize(2)))
                .andExpect(jsonPath("$.errores[*].campo",
                        org.hamcrest.Matchers.containsInAnyOrder("productos[0].sku", "productos[0].precioUnitario")));
        verifyNoInteractions(inventarioService);
    }

    @Test
    void cargarConListaVaciaDevuelve400() throws Exception {
        mvc.perform(post(URL, 1).header(VERSION, "1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productos\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("productos"));
    }

    @Test
    void cargarConUnidadDeMedidaInexistenteDevuelve400() throws Exception {
        mvc.perform(post(URL, 1).header(VERSION, "1").contentType(MediaType.APPLICATION_JSON).content("""
                        {"productos":[{"sku":"A","nombre":"A","categoria":"C",
                        "unidadMedida":"GALAXIA","precioUnitario":1,"cantidadInicial":1}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", not(containsString("Exception"))));
    }

    @Test
    void cargarSkuYaRegistradoDevuelve409() throws Exception {
        when(inventarioService.cargar(eq(1L), any()))
                .thenThrow(new ConflictoException("Los siguientes SKU ya están registrados en el almacén MED-01: SKU-1"));

        mvc.perform(post(URL, 1).header(VERSION, "1").contentType(MediaType.APPLICATION_JSON).content("""
                        {"productos":[{"sku":"SKU-1","nombre":"A","categoria":"C",
                        "unidadMedida":"UNIDAD","precioUnitario":1,"cantidadInicial":1}]}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto"))
                .andExpect(jsonPath("$.detail", containsString("SKU-1")));
    }
}
