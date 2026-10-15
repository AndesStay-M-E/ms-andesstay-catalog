package cl.duoc.andesstay.catalog.accommodation;

import cl.duoc.andesstay.catalog.accommodation.dto.AccommodationRequest;
import cl.duoc.andesstay.catalog.accommodation.dto.AccommodationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * GET    /api/catalog/accommodations?onlyActive=true   listar
 * GET    /api/catalog/accommodations/{id}              detalle
 * POST   /api/catalog/accommodations                   crear     (ADMIN, OPERADOR)
 * PUT    /api/catalog/accommodations/{id}              modificar (ADMIN, OPERADOR)
 * DELETE /api/catalog/accommodations/{id}              desactivar (ADMIN)
 */
@RestController
@RequestMapping("/api/catalog/accommodations")
public class AccommodationController {

    private final AccommodationService service;

    public AccommodationController(AccommodationService service) {
        this.service = service;
    }

    @GetMapping
    public List<AccommodationResponse> findAll(
            @RequestParam(defaultValue = "false") boolean onlyActive
    ) {
        return service.findAll(onlyActive);
    }

    @GetMapping("/{id}")
    public AccommodationResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<AccommodationResponse> create(
            @Valid @RequestBody AccommodationRequest request
    ) {
        AccommodationResponse response = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/catalog/accommodations/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public AccommodationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AccommodationRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
