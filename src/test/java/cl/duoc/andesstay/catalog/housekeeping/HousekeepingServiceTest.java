package cl.duoc.andesstay.catalog.housekeeping;

import cl.duoc.andesstay.catalog.accommodation.AccommodationService;
import cl.duoc.andesstay.catalog.exception.NonRecoverableMessageException;
import cl.duoc.andesstay.catalog.housekeeping.dto.HousekeepingTicketResponse;
import cl.duoc.andesstay.catalog.messaging.model.HousekeepingCommandPayload;
import cl.duoc.andesstay.catalog.messaging.model.MessageEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HousekeepingServiceTest {

    private HousekeepingTicketRepository repository;
    private HousekeepingService service;

    @BeforeEach
    void setUp() {
        repository = mock(HousekeepingTicketRepository.class);
        AccommodationService accommodations = mock(AccommodationService.class);
        when(accommodations.findNameOrNull(10L)).thenReturn("Cabaña Bosque Nativo");
        when(repository.save(any(HousekeepingTicket.class))).thenAnswer(call -> call.getArgument(0));

        service = new HousekeepingService(repository, accommodations);
    }

    @Test
    void guardaElTicketConElNombreDelAlojamiento() {
        MessageEnvelope<HousekeepingCommandPayload> envelope = envelope("PREPARE_ROOM");
        when(repository.findByEventId(envelope.eventId())).thenReturn(Optional.empty());

        HousekeepingTicketResponse response = service.process(envelope);

        assertThat(response.code()).isEqualTo("HK-1-PREPARE_ROOM");
        assertThat(response.accommodationName()).isEqualTo("Cabaña Bosque Nativo");
        assertThat(response.status()).isEqualTo(TicketStatus.PENDIENTE);
        verify(repository).save(any(HousekeepingTicket.class));
    }

    @Test
    void noDuplicaUnMensajeYaProcesado() {
        MessageEnvelope<HousekeepingCommandPayload> envelope = envelope("CLEAN_ROOM");
        HousekeepingTicket existing = new HousekeepingTicket();
        existing.setReservationId(1L);
        existing.setAction(HousekeepingAction.CLEAN_ROOM);
        when(repository.findByEventId(envelope.eventId())).thenReturn(Optional.of(existing));

        HousekeepingTicketResponse response = service.process(envelope);

        assertThat(response.code()).isEqualTo("HK-1-CLEAN_ROOM");
        verify(repository, never()).save(any());
    }

    @Test
    void accionDesconocidaEsErrorNoRecuperable() {
        assertThatThrownBy(() -> service.process(envelope("PINTAR")))
                .isInstanceOf(NonRecoverableMessageException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void completarDosVecesEsConflicto() {
        HousekeepingTicket ticket = new HousekeepingTicket();
        ticket.complete();

        assertThatThrownBy(ticket::complete).isInstanceOf(IllegalStateException.class);
    }

    private MessageEnvelope<HousekeepingCommandPayload> envelope(String action) {
        return new MessageEnvelope<>(
                "housekeeping.ticket",
                UUID.randomUUID(),
                Instant.now(),
                "trace-1",
                "reservation-1",
                new HousekeepingCommandPayload(1L, 10L, LocalDate.now(), LocalDate.now().plusDays(2), action)
        );
    }
}
