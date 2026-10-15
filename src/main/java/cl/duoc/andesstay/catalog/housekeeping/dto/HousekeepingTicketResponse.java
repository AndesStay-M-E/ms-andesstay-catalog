package cl.duoc.andesstay.catalog.housekeeping.dto;

import cl.duoc.andesstay.catalog.housekeeping.HousekeepingAction;
import cl.duoc.andesstay.catalog.housekeeping.HousekeepingTicket;
import cl.duoc.andesstay.catalog.housekeeping.TicketStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HousekeepingTicketResponse(
        Long id,
        String code,
        Long reservationId,
        Long accommodationId,
        String accommodationName,
        HousekeepingAction action,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        TicketStatus status,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    public static HousekeepingTicketResponse from(HousekeepingTicket ticket) {
        return new HousekeepingTicketResponse(
                ticket.getId(),
                ticket.getCode(),
                ticket.getReservationId(),
                ticket.getAccommodationId(),
                ticket.getAccommodationName(),
                ticket.getAction(),
                ticket.getCheckInDate(),
                ticket.getCheckOutDate(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getCompletedAt()
        );
    }
}
