package ntnu.idi.mushroomidentificationbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for Swagger API documentation.
 * This class sets up the OpenAPI documentation
 * for the Mushroom Identification Backend application.
 * * It is activated only in the 'dev' profile,
 * * allowing developers to view the API documentation
 * * during development.
 */
@Configuration
@Profile("dev") 
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mushroom Identification Backend API")
                        .version("1.0")
                        .description("API documentation for Mushroom Identification Backend application"));
    }
}