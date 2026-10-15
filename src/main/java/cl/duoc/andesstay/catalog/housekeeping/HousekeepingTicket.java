package cl.duoc.andesstay.catalog.housekeeping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ticket de preparación o limpieza creado a partir de un comando RabbitMQ.
 *
 * eventId es único: si el mismo mensaje llega dos veces (reintento),
 * no se crea un ticket duplicado.
 */
@Entity
@Table(name = "housekeeping_tickets")
public class HousekeepingTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_sequence")
    @SequenceGenerator(name = "ticket_sequence", sequenceName = "housekeeping_ticket_seq", allocationSize = 1)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "accommodation_name", length = 100)
    private String accommodationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HousekeepingAction action;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.PENDIENTE;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    /** Código legible del ticket, por ejemplo HK-12-PREPARE_ROOM. */
    public String getCode() {
        return "HK-%d-%s".formatted(reservationId, action);
    }

    public void complete() {
        if (status == TicketStatus.COMPLETADO) {
            throw new IllegalStateException("El ticket " + id + " ya está completado");
        }
        status = TicketStatus.COMPLETADO;
        completedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getAccommodationId() {
        return accommodationId;
    }

    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
    }

    public String getAccommodationName() {
        return accommodationName;
    }

    public void setAccommodationName(String accommodationName) {
        this.accommodationName = accommodationName;
    }

    public HousekeepingAction getAction() {
        return action;
    }

    public void setAction(HousekeepingAction action) {
        this.action = action;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
