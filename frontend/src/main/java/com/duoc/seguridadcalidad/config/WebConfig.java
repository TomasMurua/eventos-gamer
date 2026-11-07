package com.duoc.seguridadcalidad.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

@Configuration
public class WebConfig {

    /** El sitio es solo en espanol: fechas y meses siempre en es-CL, sin importar el idioma del navegador. */
    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.forLanguageTag("es-CL"));
    }
}
