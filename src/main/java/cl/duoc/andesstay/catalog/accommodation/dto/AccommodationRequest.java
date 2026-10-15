package cl.duoc.andesstay.catalog.accommodation.dto;

import cl.duoc.andesstay.catalog.accommodation.AccommodationType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos para crear o modificar un alojamiento.
 */
public record AccommodationRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @NotNull(message = "El tipo es obligatorio: HABITACION, SUITE, CABANA o DEPARTAMENTO")
        AccommodationType type,

        @NotBlank(message = "La ubicación es obligatoria")
        @Size(max = 120, message = "La ubicación no puede superar 120 caracteres")
        String location,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad mínima es 1 huésped")
        @Max(value = 20, message = "La capacidad máxima es 20 huéspedes")
        Integer capacity,

        @NotNull(message = "El precio por noche es obligatorio")
        @DecimalMin(value = "1", message = "El precio por noche debe ser mayor que cero")
        @DecimalMax(value = "10000000", message = "El precio por noche es demasiado alto")
        BigDecimal pricePerNight,

        Boolean active

) {
}
