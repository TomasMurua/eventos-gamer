package com.duoc.seguridadcalidad.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Carga en H2 los mismos scripts de db/ que usa MySQL, omitiendo las lineas propias de MySQL
 * (CREATE DATABASE y USE).
 */
@TestConfiguration
public class ScriptsBaseDeDatos {

    @Bean
    public DataSourceInitializer inicializador(DataSource dataSource) throws IOException {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        for (String archivo : new String[] {"01_schema.sql", "02_datos.sql"}) {
            String sql = Files.readAllLines(Path.of("..", "db", archivo), StandardCharsets.UTF_8).stream()
                    .filter(l -> !l.startsWith("CREATE DATABASE") && !l.startsWith("USE "))
                    .collect(Collectors.joining("\n"));
            populator.addScript(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
        }
        DataSourceInitializer init = new DataSourceInitializer();
        init.setDataSource(dataSource);
        init.setDatabasePopulator(populator);
        return init;
    }
}
