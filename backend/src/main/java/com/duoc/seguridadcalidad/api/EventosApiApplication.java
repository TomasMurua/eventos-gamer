package com.duoc.seguridadcalidad.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Sin el usuario en memoria que Spring Boot crea por defecto (y cuya clave imprime en el log)
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class EventosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventosApiApplication.class, args);
    }
}
