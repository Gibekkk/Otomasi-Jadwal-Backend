package com.jadwal.restfulapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wadah otomatis untuk @ErrorExample yang ditempel berkali-kali di 1 method.
 * Tidak perlu dipakai langsung -- cukup tulis beberapa @ErrorExample,
 * Java yang otomatis membungkusnya ke sini di belakang layar.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ErrorExamples {
    ErrorExample[] value();
}
