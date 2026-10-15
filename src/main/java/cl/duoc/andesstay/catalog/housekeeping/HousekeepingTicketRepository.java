package cl.duoc.andesstay.catalog.housekeeping;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HousekeepingTicketRepository extends JpaRepository<HousekeepingTicket, Long> {

    Optional<HousekeepingTicket> findByEventId(UUID eventId);

    List<HousekeepingTicket> findAllByOrderByCreatedAtDesc();

    List<HousekeepingTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);
}
