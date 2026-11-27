package co.edu.udea.arqsoftware.inventario.web;

import co.edu.udea.arqsoftware.inventario.service.DashboardService;
import co.edu.udea.arqsoftware.inventario.web.dto.DashboardSummaryResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/dashboard", version = "1")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true, example = "1")
    public EntityModel<DashboardSummaryResponse> summary() {
        return EntityModel.of(
                dashboardService.getSummary(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(DashboardController.class)
                        .summary()).withSelfRel());
    }
}
