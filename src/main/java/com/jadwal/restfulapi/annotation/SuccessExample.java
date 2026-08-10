package com.jadwal.restfulapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tempel di atas method controller untuk kasih contoh response sukses di
 * Swagger, tanpa perlu nulis blok @ApiResponse/@Content/@ExampleObject penuh.
 *
 * Contoh pakai:
 *
 * @SuccessExample(code = "200", value = "{\"id\":\"uuid\",\"name\":\"Contoh\"}")
 *
 * value harus JSON valid (object atau array). Response error (400/403/404/500)
 * otomatis ditambahkan sendiri oleh GlobalResponseCustomizer, jadi cukup fokus
 * ke contoh data suksesnya saja di sini.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SuccessExample {

    /** Contoh body response, dalam bentuk string JSON. */
    String value();

    /** Kode status HTTP untuk contoh ini. Default "200". */
    String code() default "200";

    /** Deskripsi singkat yang muncul di Swagger UI. */
    String description() default "Success";
}
