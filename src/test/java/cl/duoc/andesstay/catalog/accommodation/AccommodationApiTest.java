package cl.duoc.andesstay.catalog.accommodation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la API del catálogo con la seguridad real (roles del JWT).
 */
@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.datasource.url=jdbc:h2:mem:catalogapi;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class AccommodationApiTest {

    private static final String URL = "/api/catalog/accommodations";

    private static final String VALID_BODY = """
            {"name":"Cabaña Test","type":"CABANA","location":"Pucón",
             "capacity":4,"pricePerNight":70000}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clientePuedeListarAlojamientos() throws Exception {
        mockMvc.perform(get(URL).with(role("CLIENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeCrearAlojamientos() throws Exception {
        mockMvc.perform(post(URL).with(role("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void operadorCreaAlojamiento() throws Exception {
        mockMvc.perform(post(URL).with(role("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cabaña Test"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void datosInvalidosResponden400ConDetalle() throws Exception {
        mockMvc.perform(post(URL).with(role("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"type\":\"CABANA\",\"location\":\"X\",\"capacity\":0,\"pricePerNight\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.capacity").exists())
                .andExpect(jsonPath("$.errors.pricePerNight").exists());
    }

    @Test
    void operadorNoPuedeDesactivar() throws Exception {
        mockMvc.perform(delete(URL + "/1").with(role("OPERADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void alojamientoInexistenteResponde404() throws Exception {
        mockMvc.perform(get(URL + "/99999").with(role("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void ticketsSoloParaAdminUOperador() throws Exception {
        mockMvc.perform(get("/api/catalog/housekeeping-tickets").with(role("CLIENTE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/catalog/housekeeping-tickets").with(role("OPERADOR")))
                .andExpect(status().isOk());
    }

    private JwtRequestPostProcessor role(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
