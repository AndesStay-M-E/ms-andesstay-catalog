package cl.duoc.andesstay.catalog.messaging.model;

import java.time.LocalDate;

public record HousekeepingCommandPayload(
        Long reservationId,
        Long accommodationId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String action
) {
}
