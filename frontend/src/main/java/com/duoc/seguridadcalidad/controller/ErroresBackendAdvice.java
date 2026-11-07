package com.duoc.seguridadcalidad.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

/**
 * Traduce los errores de la API del backend a respuestas controladas, sin exponer detalles internos:
 * token rechazado = cerrar sesion, error del cliente = 400/403/404, backend caido = 503.
 */
@ControllerAdvice
public class ErroresBackendAdvice {

    private static final Logger log = LoggerFactory.getLogger("seguridad");

    /** Si el backend rechaza el token (expiro o fue revocado), se cierra la sesion web y se pide login otra vez. */
    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public String tokenRechazado(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login?expirada";
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public String errorDelCliente(HttpClientErrorException e, HttpServletRequest request,
                                  HttpServletResponse response, Model model) {
        int status = e.getStatusCode().value();
        log.warn("El backend rechazo la peticion: status={} ruta={}", status, request.getRequestURI());
        response.setStatus(status);
        model.addAttribute("status", status);
        return "error";
    }

    @ExceptionHandler(RestClientException.class)
    public String backendNoDisponible(RestClientException e, HttpServletResponse response, Model model) {
        log.error("No se pudo consultar el backend: {}", e.getClass().getSimpleName());
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        model.addAttribute("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        return "error";
    }
}
