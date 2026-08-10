package com.jadwal.restfulapi.config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadwal.restfulapi.annotation.NoAuth;
import com.jadwal.restfulapi.annotation.NotFoundExample;
import com.jadwal.restfulapi.annotation.SuccessExample;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;

/**
 * Jalan otomatis untuk SETIAP endpoint saat Swagger generate dokumentasi
 * (tiap kali aplikasi start / /v3/api-docs dipanggil) -- tidak perlu jalankan
 * apa pun manual saat coding.
 *
 * Yang benar-benar OTOMATIS/GLOBAL (sama di semua controller, tidak perlu
 * anotasi apa pun):
 *
 * 1. Security requirement "Token" di header, KECUALI method/class ditandai
 * @NoAuth (dipakai di AuthController#login, ImageController#getImage,
 * CustomErrorController).
 * 2. Contoh response 403 (Authentication Failed / Access Denied) untuk semua
 * endpoint yang butuh auth.
 * 3. Contoh response 400 (Bad Request) untuk endpoint yang punya
 * @RequestBody.
 * 4. Contoh response 500 (Internal Server Error) untuk semua endpoint.
 *
 * Yang di-MAPPING MANUAL per controller (beda-beda tiap endpoint, tidak bisa
 * ditebak otomatis):
 *
 * 5. Contoh response 200/201 sukses -- pakai @SuccessExample di method-nya,
 * karena bentuk data beda tiap endpoint.
 * 6. Contoh response 404 -- pakai @NotFoundExample di method-nya, karena
 * pesannya beda tiap resource ("Course Not Found", "User Not Found", dst).
 *
 * Kalau kamu sudah tulis @ApiResponse manual untuk kode tertentu di endpoint
 * itu, punya kamu tidak akan ditimpa (dicek lewat responses.containsKey).
 */
@Component
public class GlobalResponseCustomizer implements OperationCustomizer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        boolean noAuth = handlerMethod.hasMethodAnnotation(NoAuth.class)
                || handlerMethod.getBeanType().isAnnotationPresent(NoAuth.class);

        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        SuccessExample success = handlerMethod.getMethodAnnotation(SuccessExample.class);
        if (success != null) {
            addJsonExample(responses, success.code(), success.description(), success.value(), false);
        }

        if (!noAuth) {
            operation.addSecurityItem(new SecurityRequirement().addList(OpenApiConfig.SECURITY_SCHEME_NAME));
            addErrorExample(responses, "403", "Authentication Failed / Access Denied",
                    "Forbidden", "You do not have permission to access this resource",
                    "Authentication Failed");
        }

        NotFoundExample notFound = handlerMethod.getMethodAnnotation(NotFoundExample.class);
        if (notFound != null) {
            addErrorExample(responses, "404", "Resource Not Found",
                    "Not Found", "The requested resource could not be found",
                    notFound.value());
        }

        boolean hasBody = Arrays.stream(handlerMethod.getMethodParameters())
                .anyMatch(p -> p.hasParameterAnnotation(RequestBody.class));
        if (hasBody) {
            addErrorExample(responses, "400", "Invalid Request Body",
                    "Bad Request", "Malformed request syntax",
                    "Field is Required");
        }

        addErrorExample(responses, "500", "Unexpected Server Error",
                "Internal Server Error", "An unexpected error occurred on the server",
                "An unexpected error occurred on the server");

        return operation;
    }

    private void addErrorExample(ApiResponses responses, String code, String description,
            String httpMessage, String detail, String message) {
        if (responses.containsKey(code)) {
            return;
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("HTTPMessage", httpMessage);
        json.put("detail", detail);
        json.put("message", message);
        addExample(responses, code, description, json, true);
    }

    private void addJsonExample(ApiResponses responses, String code, String description,
            String rawJson, boolean useErrorSchema) {
        Object parsed;
        try {
            parsed = mapper.readValue(rawJson, Object.class);
        } catch (Exception e) {
            parsed = rawJson;
        }
        addExample(responses, code, description, parsed, useErrorSchema);
    }

    private void addExample(ApiResponses responses, String code, String description,
            Object value, boolean useErrorSchema) {
        Example example = new Example();
        example.setValue(value);

        Schema<?> schema = useErrorSchema
                ? new Schema<>().$ref("#/components/schemas/" + OpenApiConfig.ERROR_SCHEMA_NAME)
                : new Schema<>().type("object");

        MediaType mediaType = new MediaType()
                .schema(schema)
                .addExamples(code + "-example", example);

        Content content = new Content().addMediaType("application/json", mediaType);

        responses.addApiResponse(code, new ApiResponse()
                .description(description)
                .content(content));
    }
}
