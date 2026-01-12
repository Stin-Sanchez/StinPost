package com.stinjoss.springbootmvc.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtener directorio raíz del proyecto
        String projectRoot = System.getProperty("user.dir");
        String absolutePath = Paths.get(projectRoot, uploadDir).toString();

        System.out.println(" CONFIGURANDO RECURSOS ESTÁTICOS");
        System.out.println(" Proyecto: " + projectRoot);
        System.out.println(" Upload absoluto: " + absolutePath);

        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + absolutePath + "/")
                .setCacheControl(CacheControl.noCache());

        System.out.println(" Configuración completa con éxito");
    }
}
