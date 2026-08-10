package com.jadwal.restfulapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jadwal.restfulapi.util.ErrorMessage;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Konfigurasi dasar Swagger/OpenAPI.
 *
 * - Daftarkan security scheme "TokenAuth": apiKey di header "Token", sesuai
 *   cara AuthController & controller lain baca request.getHeader("Token").
 *   Ini BUKAN "Authorization: Bearer", jadi tidak dipakai skema bearer bawaan.
 * - Daftarkan schema ErrorMessage supaya bisa dipakai $ref oleh
 *   GlobalResponseCustomizer untuk contoh response error.
 *
 * Siapa yang pakai security ini per endpoint diatur otomatis oleh
 * GlobalResponseCustomizer (lihat kelas itu), bukan di sini.
 */
@Configuration
public class OpenApiConfig {

    public static final String ERROR_SCHEMA_NAME = "ErrorMessage";
    public static final String SECURITY_SCHEME_NAME = "TokenAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        ResolvedSchema resolved = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ErrorMessage.class));

        Components components = new Components()
                .addSchemas(ERROR_SCHEMA_NAME, resolved.schema)
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("Token")
                        .description(
                                "Session Token sent via HTTP Header. "
                                        + "Get it from /api/auth/login."));

        if (resolved.referencedSchemas != null) {
            resolved.referencedSchemas.forEach(components::addSchemas);
        }

        return new OpenAPI()
                .info(new Info()
                        .title("Otomasi Jadwal API")
                        .version("v1")
                        .description("Dokumentasi REST API Otomasi Penjadwalan. "
                                + "Endpoint bergembok butuh header Token, klik Authorize lalu isi token-nya."))
                .components(components);
    }
}
