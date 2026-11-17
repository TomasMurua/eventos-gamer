package com.duoc.seguridadcalidad.api.error;

import com.duoc.seguridadcalidad.api.dto.ErrorRespuesta;
import com.duoc.seguridadcalidad.api.dto.ErrorValidacion;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Convierte los errores esperables en respuestas JSON cortas. Nunca devuelve trazas, nombres de clases
 * ni el valor que mando el usuario.
 */
@RestControllerAdvice
public class ErroresApi {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorValidacion> camposInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError campo : e.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(campo.getField(), campo.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ErrorValidacion(400, "Hay datos inválidos", errores));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorRespuesta> parametrosInvalidos(HandlerMethodValidationException e) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta(400, "Hay parámetros inválidos"));
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorRespuesta> reglaDeNegocio(SolicitudInvalidaException e) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta(400, e.getMessage()));
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorRespuesta> conflicto(ConflictoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorRespuesta(409, e.getMessage()));
    }

    @ExceptionHandler(DemasiadasSolicitudesException.class)
    public ResponseEntity<ErrorRespuesta> demasiadas(DemasiadasSolicitudesException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorRespuesta(429, e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorRespuesta> archivoGrande(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorRespuesta(413, "El archivo supera el tamaño permitido"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorRespuesta> multipartInvalido(MultipartException e) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta(400, "No se pudo leer el archivo enviado"));
    }
}
