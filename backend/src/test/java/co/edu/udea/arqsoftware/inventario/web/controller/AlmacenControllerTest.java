package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.AlmacenService;
import co.edu.udea.arqsoftware.inventario.service.exception.ConflictoException;
import co.edu.udea.arqsoftware.inventario.service.exception.RecursoNoEncontradoException;
import co.edu.udea.arqsoftware.inventario.web.assembler.AlmacenModelAssembler;
import co.edu.udea.arqsoftware.inventario.web.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static co.edu.udea.arqsoftware.inventario.TestData.almacenResponse;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlmacenController.class)
@Import(AlmacenModelAssembler.class)
class AlmacenControllerTest {

    private static final String VERSION = "X-API-Version";
    private static final String CUERPO = """
            {"codigoSede":"BAQ-01","nombre":"Almacén Barranquilla","ciudad":"Barranquilla","direccion":"Calle 72 # 50-10"}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AlmacenService almacenService;

    @Test
    void crearDevuelve201ConLocationYEnlaces() throws Exception {
        when(almacenService.crear(any())).thenReturn(almacenResponse(4L, "BAQ-01"));

        mvc.perform(post("/api/almacenes").header(VERSION, "1").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/almacenes/4")))
                .andExpect(jsonPath("$.codigoSede").value("BAQ-01"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/almacenes/4")))
                .andExpect(jsonPath("$._links.inventario.href", endsWith("/api/almacenes/4/inventario")))
                .andExpect(jsonPath("$._links.almacenes.href").exists());
    }

    @Test
    void crearConCodigoInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/almacenes").header(VERSION, "1").contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO.replace("BAQ-01", "baq 01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("codigoSede"));
    }

    @Test
    void crearConCodigoDuplicadoDevuelve409() throws Exception {
        when(almacenService.crear(any())).thenThrow(new ConflictoException("Ya existe un almacén con código de sede BAQ-01"));

        mvc.perform(post("/api/almacenes").header(VERSION, "1").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isConflict());
    }

    @Test
    void listarDevuelvePaginaHal() throws Exception {
        when(almacenService.listar(any(Pageable.class))).thenReturn(
                new PageResponse<>(List.of(almacenResponse(1L, "MED-01"), almacenResponse(2L, "BOG-01")), 0, 10, 2, 1));

        mvc.perform(get("/api/almacenes").header(VERSION, "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.almacenes", hasSize(2)))
                .andExpect(jsonPath("$._embedded.almacenes[1].codigoSede").value("BOG-01"))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/almacenes")))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void obtenerSedeInexistenteDevuelve404() throws Exception {
        when(almacenService.obtener(99L)).thenThrow(new RecursoNoEncontradoException("Almacén", 99L));

        mvc.perform(get("/api/almacenes/99").header(VERSION, "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"));
    }

    @Test
    void obtenerConIdNoNumericoDevuelve400() throws Exception {
        mvc.perform(get("/api/almacenes/abc").header(VERSION, "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("sedeId")));
    }
}
