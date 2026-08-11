package com.jadwal.restfulapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tempel BERKALI-KALI di method controller, satu @ErrorExample untuk satu
 * skenario error yang benar-benar ada di kode (satu baris
 * "data = new ErrorMessage(httpCode, "...")" di controller = satu
 * @ErrorExample di sini).
 *
 * HTTPMessage dan detail di Swagger diambil OTOMATIS dari
 * com.jadwal.restfulapi.util.HTTPCode (enum yang sama dipakai controller
 * lewat constructor ErrorMessage), jadi selalu sinkron kalau enum itu
 * berubah. Kamu cuma isi code dan "message" persis seperti argumen kedua
 * "new ErrorMessage(httpCode, "...")" di kode aslinya.
 *
 * PENTING: perhatikan kode HTTP asli tiap skenario, jangan ditebak dari pola
 * umum. Di project ini kebanyakan endpoint balas 401 untuk sesi/token tidak
 * valid, TAPI UserController#createUser dan #editUser balas 403 untuk kasus
 * yang sama -- ikuti apa yang benar-benar dilempar controller-nya.
 *
 * Contoh:
 * @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
 * @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
 * @ErrorExample(code = "404", name = "not-found", message = "Course Not Found")
 *
 * "name" = label skenario di dropdown Swagger UI, harus unik per code dalam
 * 1 method. Kalau 1 endpoint punya 2 pesan berbeda untuk code yang sama
 * (mis. 2 varian 403), kasih "name" beda -- keduanya tetap muncul sebagai
 * pilihan contoh response yang berbeda untuk code yang sama.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ErrorExamples.class)
public @interface ErrorExample {
    String code();
    String name();
    String message();
}
