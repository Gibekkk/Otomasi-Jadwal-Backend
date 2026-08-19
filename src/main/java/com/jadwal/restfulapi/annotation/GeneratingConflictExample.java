package com.jadwal.restfulapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tempel di method controller ber-method non-GET (POST/PUT/PATCH/DELETE),
 * supaya Swagger menampilkan contoh response 409 yang dilempar
 * RequestInterceptor saat FreeTable.isGenerating = true.
 *
 * Contoh pakai:
 *
 * @GeneratingConflictExample
 *
 * Message default sudah sama persis dengan yang ditulis di
 * RequestInterceptor#preHandle. Override lewat value() kalau perlu beda.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GeneratingConflictExample {
    String message() default "Generation is In Process, Write Process Are Halted";
}