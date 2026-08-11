package com.jadwal.restfulapi.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadwal.restfulapi.annotation.ErrorExample;
import com.jadwal.restfulapi.annotation.NoAuth;
import com.jadwal.restfulapi.annotation.SuccessExample;
import com.jadwal.restfulapi.util.HTTPCode;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;

/**
 * Jalan otomatis untuk SETIAP endpoint saat Swagger generate dokumentasi --
 * tidak perlu dijalankan manual saat coding, cukup tempel anotasi di
 * controller dan restart aplikasi (atau reload /v3/api-docs).
 *
 * OTOMATIS/GLOBAL, tidak perlu anotasi apa pun (berlaku sama persis di semua
 * controller karena polanya identik di kode):
 *
 * 1. Security requirement "Token" di header, KECUALI method/class ditandai
 * @NoAuth.
 * 2. Contoh 400 "missing-token": kalau header Token tidak dikirim sama
 * sekali, authService.findSessionBySessionToken(null) memanggil
 * repository.findById(null) yang otomatis lempar IllegalArgumentException
 * ("The given id must not be null!") -- ini SELALU jadi salah satu
 * kemungkinan 400 di endpoint mana pun yang butuh Token, jadi aman
 * di-generate otomatis.
 * 3. Contoh 500 generik, HANYA kalau endpoint itu tidak punya @ErrorExample
 * code 500 sendiri (kalau ada yang lebih spesifik, itu yang dipakai).
 *
 * DI-MAPPING MANUAL per method lewat anotasi (karena beda-beda tiap
 * controller, bahkan ada endpoint yang pakai kode HTTP berbeda untuk pesan
 * yang sama persis -- lihat javadoc @ErrorExample):
 *
 * 4. @SuccessExample -> isi response sukses (200/dst).
 * 5. @ErrorExample (boleh berkali-kali) -> semua skenario error lain: sesi
 * tidak valid (401 di hampir semua endpoint, TAPI 403 di
 * UserController#createUser/#editUser), akses ditolak (403), resource tidak
 * ketemu (404), body tidak valid (400), dan kasus khusus lain (mis. 401 di
 * AuthController#login, "Group ID Invalid" / "Category Not Permitted").
 *
 * Kalau nama skenario ("name") sama dipakai lagi untuk code yang sama,
 * yang belakangan menang (dipakai supaya @ErrorExample eksplisit bisa
 * menimpa contoh 500 generik di atas kalau perlu).
 */
@Component
public class GlobalResponseCustomizer implements OperationCustomizer {

    private static final String GENERIC_500_NAME = "unexpected";

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
            addSuccessExample(responses, success.code(), success.value());
        }

        if (!noAuth) {
            operation.addSecurityItem(new SecurityRequirement().addList(OpenApiConfig.SECURITY_SCHEME_NAME));
            addHttpCodeExample(responses, "400", "missing-token", "The given id must not be null!");
        }

        addHttpCodeExample(responses, "500", GENERIC_500_NAME, "An unexpected error occurred on the server");

        for (ErrorExample ex : handlerMethod.getMethod().getAnnotationsByType(ErrorExample.class)) {
            addHttpCodeExample(responses, ex.code(), ex.name(), ex.message());
        }

        return operation;
    }

    private void addHttpCodeExample(ApiResponses responses, String code, String name, String message) {
        HTTPCode httpCode;
        try {
            httpCode = HTTPCode.fromCode(Integer.parseInt(code));
        } catch (Exception e) {
            return;
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("HTTPMessage", httpCode.getReasonPhrase());
        json.put("detail", httpCode.getDetailMessage());
        json.put("message", message);

        ApiResponse response = getOrCreateResponse(responses, code, httpCode.getReasonPhrase(), true);
        putExample(response, name, json);
    }

    private void addSuccessExample(ApiResponses responses, String code, String rawJson) {
        Object parsed;
        try {
            parsed = mapper.readValue(rawJson, Object.class);
        } catch (Exception e) {
            parsed = rawJson;
        }
        ApiResponse response = getOrCreateResponse(responses, code, "Success", false);
        putExample(response, code + "-example", parsed);
    }

    private ApiResponse getOrCreateResponse(ApiResponses responses, String code, String description,
            boolean useErrorSchema) {
        ApiResponse response = responses.get(code);
        if (response != null) {
            return response;
        }

        Schema<?> schema = useErrorSchema
                ? new Schema<>().$ref("#/components/schemas/" + OpenApiConfig.ERROR_SCHEMA_NAME)
                : new Schema<>().type("object");

        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("application/json", mediaType);
        response = new ApiResponse().description(description).content(content);
        responses.addApiResponse(code, response);
        return response;
    }

    private void putExample(ApiResponse response, String name, Object value) {
        Example example = new Example();
        example.setValue(value);
        response.getContent().get("application/json").addExamples(name, example);
    }
}
