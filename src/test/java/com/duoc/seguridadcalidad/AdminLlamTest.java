package com.duoc.seguridadcalidad;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.usuarios.password-llam=ClaveDePrueba123")
@AutoConfigureMockMvc
class AdminLlamTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void llamIngresaComoAdmin() throws Exception {
        mvc.perform(formLogin("/login").user("llam").password("ClaveDePrueba123"))
                .andExpect(authenticated().withUsername("llam").withRoles("USER", "ADMIN"));
    }

    @Test
    void llamConClaveIncorrectaNoIngresa() throws Exception {
        mvc.perform(formLogin("/login").user("llam").password("otra-clave"))
                .andExpect(unauthenticated());
    }
}
