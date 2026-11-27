package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.web.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

public final class PageRequestFactory {
    private static final int MAX_PAGE_SIZE = 100;

    private PageRequestFactory() {
    }

    public static PageRequest create(int page, int size, String sort) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "page debe ser >= 0 y size debe estar entre 1 y " + MAX_PAGE_SIZE + ".");
        }
        return PageRequest.of(page, size, Sort.by(sort).ascending());
    }
}
