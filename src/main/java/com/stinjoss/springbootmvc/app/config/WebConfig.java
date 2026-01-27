package com.stinjoss.springbootmvc.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class WebConfig {
    // Esta clase habilita la serialización estable de objetos Page<?> a JSON.
    // Elimina el warning: "Serializing PageImpl instances as-is is not supported"
}
