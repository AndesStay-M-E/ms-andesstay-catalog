package cl.duoc.andesstay.catalog.housekeeping;

import cl.duoc.andesstay.catalog.exception.NonRecoverableMessageException;
import cl.duoc.andesstay.catalog.messaging.model.HousekeepingCommandPayload;
import cl.duoc.andesstay.catalog.messaging.model.MessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Crea tickets de preparación o limpieza de habitaciones.
 * El registro del ticket queda simulado con un código y un log.
 */
@Service
public class HousekeepingService {

    private static final Logger log =
            LoggerFactory.getLogger(HousekeepingService.class);

    private static final Set<String> ACTIONS =
            Set.of("PREPARE_ROOM", "CLEAN_ROOM");

    public String process(MessageEnvelope<HousekeepingCommandPayload> envelope) {
        HousekeepingCommandPayload payload = envelope.requirePayload();

        if (payload.reservationId() == null || payload.accommodationId() == null) {
            throw new NonRecoverableMessageException(
                    "El ticket no indica reserva o alojamiento"
            );
        }

        if (!ACTIONS.contains(payload.action())) {
            throw new NonRecoverableMessageException(
                    "Acción de limpieza desconocida: " + payload.action()
            );
        }

        if (payload.checkInDate() == null
                || payload.checkOutDate() == null
                || !payload.checkOutDate().isAfter(payload.checkInDate())) {
            throw new NonRecoverableMessageException(
                    "Fechas de estadía inválidas en el ticket"
            );
        }

        String ticketCode = "HK-%d-%s".formatted(
                payload.reservationId(),
                payload.action()
        );

        log.info(
                "Ticket creado codigo={} alojamiento={} accion={} entrada={} salida={} traceId={}",
                ticketCode,
                payload.accommodationId(),
                payload.action(),
                payload.checkInDate(),
                payload.checkOutDate(),
                envelope.traceId()
        );

        return ticketCode;
    }
}
