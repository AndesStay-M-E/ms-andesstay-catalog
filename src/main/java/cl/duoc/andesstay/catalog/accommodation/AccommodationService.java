package cl.duoc.andesstay.catalog.accommodation;

import cl.duoc.andesstay.catalog.accommodation.dto.AccommodationRequest;
import cl.duoc.andesstay.catalog.accommodation.dto.AccommodationResponse;
import cl.duoc.andesstay.catalog.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio del catálogo de alojamientos.
 */
@Service
@Transactional
public class AccommodationService {

    private static final Logger log =
            LoggerFactory.getLogger(AccommodationService.class);

    private final AccommodationRepository repository;

    public AccommodationService(AccommodationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponse> findAll(boolean onlyActive) {
        List<Accommodation> accommodations = onlyActive
                ? repository.findByActiveTrueOrderByNameAsc()
                : repository.findAllByOrderByNameAsc();

        return accommodations.stream()
                .map(AccommodationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccommodationResponse findById(Long id) {
        return AccommodationResponse.from(findEntity(id));
    }

    public AccommodationResponse create(AccommodationRequest request) {
        Accommodation accommodation = new Accommodation();
        apply(accommodation, request);

        Accommodation saved = repository.save(accommodation);
        log.info("Alojamiento creado id={} nombre={}", saved.getId(), saved.getName());

        return AccommodationResponse.from(saved);
    }

    public AccommodationResponse update(Long id, AccommodationRequest request) {
        Accommodation accommodation = findEntity(id);
        apply(accommodation, request);

        Accommodation saved = repository.save(accommodation);
        log.info("Alojamiento actualizado id={}", saved.getId());

        return AccommodationResponse.from(saved);
    }

    /**
     * Baja lógica: el alojamiento queda inactivo, pero se conserva porque
     * las reservas existentes lo referencian por su id.
     */
    public void deactivate(Long id) {
        Accommodation accommodation = findEntity(id);

        if (!accommodation.isActive()) {
            throw new IllegalStateException("El alojamiento " + id + " ya está inactivo");
        }

        accommodation.setActive(false);
        repository.save(accommodation);
        log.warn("Alojamiento desactivado id={}", id);
    }

    @Transactional(readOnly = true)
    public String findNameOrNull(Long id) {
        return id == null
                ? null
                : repository.findById(id).map(Accommodation::getName).orElse(null);
    }

    private Accommodation findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el alojamiento con ID " + id));
    }

    private void apply(Accommodation accommodation, AccommodationRequest request) {
        accommodation.setName(request.name().trim());
        accommodation.setType(request.type());
        accommodation.setLocation(request.location().trim());
        accommodation.setDescription(
                request.description() == null || request.description().isBlank()
                        ? null
                        : request.description().trim());
        accommodation.setCapacity(request.capacity());
        accommodation.setPricePerNight(request.pricePerNight());
        accommodation.setActive(request.active() == null || request.active());
    }
}
