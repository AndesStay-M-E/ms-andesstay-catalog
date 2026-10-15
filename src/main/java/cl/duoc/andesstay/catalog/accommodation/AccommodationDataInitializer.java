package cl.duoc.andesstay.catalog.accommodation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Carga alojamientos de ejemplo en el perfil local, solo si el catálogo está vacío.
 */
@Component
@Profile("local")
public class AccommodationDataInitializer implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(AccommodationDataInitializer.class);

    private final AccommodationRepository repository;

    public AccommodationDataInitializer(AccommodationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }

        repository.save(build("Habitación Doble Volcán", AccommodationType.HABITACION, "Pucón",
                "Vista al volcán Villarrica", 2, "65000"));
        repository.save(build("Suite Lago", AccommodationType.SUITE, "Puerto Varas",
                "Suite con terraza frente al lago Llanquihue", 2, "120000"));
        repository.save(build("Cabaña Bosque Nativo", AccommodationType.CABANA, "Malalcahuello",
                "Cabaña con tinaja y calefacción a leña", 5, "95000"));
        repository.save(build("Departamento Cordillera", AccommodationType.DEPARTAMENTO, "San Pedro de Atacama",
                "Departamento con cocina equipada", 4, "80000"));

        log.info("Catálogo inicial cargado: {} alojamientos", repository.count());
    }

    private Accommodation build(String name, AccommodationType type, String location,
                                String description, int capacity, String price) {
        Accommodation accommodation = new Accommodation();
        accommodation.setName(name);
        accommodation.setType(type);
        accommodation.setLocation(location);
        accommodation.setDescription(description);
        accommodation.setCapacity(capacity);
        accommodation.setPricePerNight(new BigDecimal(price));
        return accommodation;
    }
}
