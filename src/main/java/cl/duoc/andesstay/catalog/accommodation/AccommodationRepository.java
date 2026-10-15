package cl.duoc.andesstay.catalog.accommodation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    List<Accommodation> findAllByOrderByNameAsc();

    List<Accommodation> findByActiveTrueOrderByNameAsc();
}
