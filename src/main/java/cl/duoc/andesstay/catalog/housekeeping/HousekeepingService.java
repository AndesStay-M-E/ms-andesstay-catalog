package cl.duoc.andesstay.catalog.housekeeping;

import cl.duoc.andesstay.catalog.accommodation.AccommodationService;
import cl.duoc.andesstay.catalog.exception.NonRecoverableMessageException;
import cl.duoc.andesstay.catalog.exception.ResourceNotFoundException;
import cl.duoc.andesstay.catalog.housekeeping.dto.HousekeepingTicketResponse;
import cl.duoc.andesstay.catalog.messaging.model.HousekeepingCommandPayload;
import cl.duoc.andesstay.catalog.messaging.model.MessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Tickets de preparación y limpieza de alojamientos.
 *
 * process() se invoca desde el consumidor RabbitMQ; el resto, desde la API REST.
 * Esta clase no conoce RabbitMQ: solo recibe el sobre ya convertido.
 */
@Service
@Transactional
public class HousekeepingService {

    private static final Logger log =
            LoggerFactory.getLogger(HousekeepingService.class);

    private final HousekeepingTicketRepository repository;
    private final AccommodationService accommodationService;

    public HousekeepingService(
            HousekeepingTicketRepository repository,
            AccommodationService accommodationService
    ) {
        this.repository = repository;
        this.accommodationService = accommodationService;
    }

    /**
     * Crea el ticket a partir del comando. Si el mensaje ya fue procesado
     * (mismo eventId), devuelve el ticket existente sin duplicarlo.
     *
     * Errores de datos -> NonRecoverableMessageException (va a la DLQ).
     * Errores de base de datos -> se propagan y el consumidor reintenta.
     */
    public HousekeepingTicketResponse process(MessageEnvelope<HousekeepingCommandPayload> envelope) {
        HousekeepingCommandPayload payload = envelope.requirePayload();
        validate(envelope, payload);

        Optional<HousekeepingTicket> existing = repository.findByEventId(envelope.eventId());
        if (existing.isPresent()) {
            log.warn("Mensaje {} ya procesado: ticket {} existente", envelope.reference(), existing.get().getCode());
            return HousekeepingTicketResponse.from(existing.get());
        }

        HousekeepingTicket ticket = new HousekeepingTicket();
        ticket.setEventId(envelope.eventId());
        ticket.setReservationId(payload.reservationId());
        ticket.setAccommodationId(payload.accommodationId());
        ticket.setAccommodationName(accommodationService.findNameOrNull(payload.accommodationId()));
        ticket.setAction(parseAction(payload.action()));
        ticket.setCheckInDate(payload.checkInDate());
        ticket.setCheckOutDate(payload.checkOutDate());
        ticket.setTraceId(envelope.traceId());

        HousekeepingTicket saved = repository.save(ticket);

        log.info(
                "Ticket creado codigo={} id={} alojamiento={} accion={} entrada={} salida={} traceId={}",
                saved.getCode(),
                saved.getId(),
                saved.getAccommodationId(),
                saved.getAction(),
                saved.getCheckInDate(),
                saved.getCheckOutDate(),
                envelope.traceId()
        );

        return HousekeepingTicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<HousekeepingTicketResponse> findAll(TicketStatus status) {
        List<HousekeepingTicket> tickets = status == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status);

        return tickets.stream()
                .map(HousekeepingTicketResponse::from)
                .toList();
    }

    public HousekeepingTicketResponse complete(Long id) {
        HousekeepingTicket ticket = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el ticket con ID " + id));

        ticket.complete();
        log.info("Ticket completado codigo={} id={}", ticket.getCode(), id);

        return HousekeepingTicketResponse.from(repository.save(ticket));
    }

    private void validate(
            MessageEnvelope<HousekeepingCommandPayload> envelope,
            HousekeepingCommandPayload payload
    ) {
        if (envelope.eventId() == null) {
            throw new NonRecoverableMessageException("El mensaje no trae eventId");
        }

        if (payload.reservationId() == null || payload.accommodationId() == null) {
            throw new NonRecoverableMessageException("El ticket no indica reserva o alojamiento");
        }

        if (payload.checkInDate() == null
                || payload.checkOutDate() == null
                || !payload.checkOutDate().isAfter(payload.checkInDate())) {
            throw new NonRecoverableMessageException("Fechas de estadía inválidas en el ticket");
        }
    }

    private HousekeepingAction parseAction(String action) {
        try {
            return HousekeepingAction.valueOf(action);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new NonRecoverableMessageException("Acción de limpieza desconocida: " + action);
        }
    }
}
