package com.mousty.convify_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI convifyOpenAPI() {
        Server server = new Server();
        server.setUrl(serverUrl);
        server.setDescription("Server URL");

        Contact contact = new Contact();
        contact.setName("Convify API Support");
        contact.setEmail("support@convify.com");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Convify YouTube Converter API")
                .version("1.0")
                .contact(contact)
                .description("API for converting YouTube videos to MP3 or MP4 format")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
