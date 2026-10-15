package cl.duoc.andesstay.catalog.accommodation.dto;

import cl.duoc.andesstay.catalog.accommodation.Accommodation;
import cl.duoc.andesstay.catalog.accommodation.AccommodationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccommodationResponse(
        Long id,
        String name,
        AccommodationType type,
        String location,
        String description,
        Integer capacity,
        BigDecimal pricePerNight,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AccommodationResponse from(Accommodation accommodation) {
        return new AccommodationResponse(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getType(),
                accommodation.getLocation(),
                accommodation.getDescription(),
                accommodation.getCapacity(),
                accommodation.getPricePerNight(),
                accommodation.isActive(),
                accommodation.getCreatedAt(),
                accommodation.getUpdatedAt()
        );
    }
}
