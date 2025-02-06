package ntnu.idi.mushroomidentificationbackend.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")  // Enable Swagger only in the 'dev' profile
@OpenAPIDefinition(
    info = @Info(
        title = "Mushroom Identification API",
        version = "1.0",
        description = "API for mushroom identification and related tasks",
        contact = @Contact(name = "Your Name", email = "your.email@example.com")
    )
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/**") // This will include all endpoints
                .build();
    }
}
