package com.duoc.seguridadcalidad.api.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

class ErroresApiTest {

    private final ErroresApi errores = new ErroresApi();

    @Test
    void erroresDeArchivoNoExponenDetallesInternos() {
        var grande = errores.archivoGrande(new MaxUploadSizeExceededException(8));
        assertThat(grande.getStatusCode().value()).isEqualTo(413);
        assertThat(grande.getBody().mensaje()).isEqualTo("El archivo supera el tamaño permitido");

        var roto = errores.multipartInvalido(new MultipartException("detalle interno de Tomcat"));
        assertThat(roto.getStatusCode().value()).isEqualTo(400);
        assertThat(roto.getBody().mensaje()).doesNotContain("Tomcat");
    }
}
