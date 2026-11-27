package co.edu.udea.arqsoftware.inventario.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

class ApiVersionFilterTest {

    private final ApiVersionFilter filter = new ApiVersionFilter(JsonMapper.builder().build());

    @Test
    void leavesSpanishApiVersioningToSpringMvc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/almacenes");
        request.addHeader(ApiVersionFilter.HEADER_NAME, "2");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNotNull(chain.getRequest());
    }

    @Test
    void requiresVersionForSpanishApiRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/almacenes");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(400, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void letsCorsPreflightReachSpringMvcWithoutVersionHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/products");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNotNull(chain.getRequest());
    }

    @Test
    void rejectsUnsupportedVersionForCompatibilityApi() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        request.addHeader(ApiVersionFilter.HEADER_NAME, "2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(406, response.getStatus());
        assertEquals("application/problem+json", response.getContentType());
        assertNull(chain.getRequest());
    }
}
