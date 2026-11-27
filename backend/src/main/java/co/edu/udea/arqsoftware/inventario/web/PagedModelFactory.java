package co.edu.udea.arqsoftware.inventario.web;

import java.util.List;
import java.util.function.IntFunction;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;

public final class PagedModelFactory {
    private PagedModelFactory() {
    }

    public static <T> PagedModel<EntityModel<T>> from(
            Page<?> page,
            List<EntityModel<T>> content,
            IntFunction<Link> pageLink) {
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages());
        PagedModel<EntityModel<T>> model = PagedModel.of(content, metadata);
        model.add(pageLink.apply(page.getNumber()).withRel("self"));
        model.add(pageLink.apply(0).withRel("first"));
        if (page.hasPrevious()) {
            model.add(pageLink.apply(page.getNumber() - 1).withRel("prev"));
        }
        if (page.hasNext()) {
            model.add(pageLink.apply(page.getNumber() + 1).withRel("next"));
        }
        model.add(pageLink.apply(Math.max(page.getTotalPages() - 1, 0)).withRel("last"));
        return model;
    }
}
