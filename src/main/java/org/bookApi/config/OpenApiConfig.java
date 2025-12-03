package org.bookApi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library API")
                        .version("1.0.0")
                        .description("""
                                Це API для управління книгами та авторами. 
                                Можна створювати, оновлювати, видаляти та шукати записи.

                                **Щоб перевірити весь функціонал книг, спочатку треба виконати ендпоінт:**
                                `POST http://localhost:8080/api/book/_upload` та завантажити книги з файлу `upload.json`.
                                """)
                );
    }
}
