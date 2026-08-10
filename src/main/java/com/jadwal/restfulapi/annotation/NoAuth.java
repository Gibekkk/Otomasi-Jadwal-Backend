package com.jadwal.restfulapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tandai method (atau seluruh controller) yang TIDAK butuh header "Token".
 * Contoh: AuthController#login, ImageController#getImage.
 *
 * Endpoint tanpa anotasi ini otomatis dianggap butuh header Token oleh
 * GlobalResponseCustomizer, dan Swagger UI akan menampilkan gembok + tombol
 * Authorize untuk endpoint tersebut.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface NoAuth {
}
