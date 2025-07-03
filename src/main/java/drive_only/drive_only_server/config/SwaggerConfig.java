package drive_only.drive_only_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi driveOnlyApi() {
        return GroupedOpenApi.builder()
                .group("drive-only-api")
                .pathsToMatch("/api/**") // /api로 시작하는 경로만 Swagger 문서에 포함
                .build();
    }

    @Bean
    public OpenAPI driveOnlyOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://drive-only.com"))
                .info(new Info()
                        .title("Drive-Only API")
                        .version("v1")
                        .description("Drive-Only API 명세서"));
    }
}
