package com.jadwal.restfulapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tempel di method controller yang bisa balas 404, isi pesan sesuai resource
 * masing-masing (beda tiap controller), contoh:
 *
 * @NotFoundExample("Course Not Found")
 * @NotFoundExample("User Not Found")
 *
 * Beda dengan 500/400/403 yang otomatis global, 404 SENGAJA tidak ditebak
 * otomatis (tidak semua endpoint ber-@PathVariable itu artinya 404, dan
 * pesannya beda-beda), jadi harus ditandai manual per endpoint.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NotFoundExample {
    String value();
}
