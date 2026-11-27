package co.edu.udea.arqsoftware.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.udea.arqsoftware.inventario.web.ApiException;
import org.junit.jupiter.api.Test;

class PageRequestFactoryTest {
    @Test
    void createsZeroBasedPageWithRequestedSort() {
        var page = PageRequestFactory.create(2, 20, "nombre");

        assertEquals(2, page.getPageNumber());
        assertEquals(20, page.getPageSize());
        assertEquals("nombre", page.getSort().getOrderFor("nombre").getProperty());
    }

    @Test
    void rejectsPageSizesAboveApiLimit() {
        assertThrows(ApiException.class, () -> PageRequestFactory.create(0, 101, "nombre"));
    }
}
