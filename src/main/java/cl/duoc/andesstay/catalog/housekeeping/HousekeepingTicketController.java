package cl.duoc.andesstay.catalog.housekeeping;

import cl.duoc.andesstay.catalog.housekeeping.dto.HousekeepingTicketResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET   /api/catalog/housekeeping-tickets?status=PENDIENTE   listar tickets
 * PATCH /api/catalog/housekeeping-tickets/{id}/complete      marcar como completado
 *
 * Ambos requieren rol ADMIN u OPERADOR.
 */
@RestController
@RequestMapping("/api/catalog/housekeeping-tickets")
public class HousekeepingTicketController {

    private final HousekeepingService service;

    public HousekeepingTicketController(HousekeepingService service) {
        this.service = service;
    }

    @GetMapping
    public List<HousekeepingTicketResponse> findAll(
            @RequestParam(required = false) TicketStatus status
    ) {
        return service.findAll(status);
    }

    @PatchMapping("/{id}/complete")
    public HousekeepingTicketResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }
}
