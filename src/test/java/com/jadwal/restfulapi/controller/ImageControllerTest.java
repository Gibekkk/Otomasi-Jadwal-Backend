package com.jadwal.restfulapi.controller;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters
@TestPropertySource(properties = {
        "storage.api-prefix=/api" // Mensimulasikan variabel application.properties
})
@DisplayName("ImageController Web Layer Tests")
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Tambahan MockBean untuk mencegah crash akibat dependensi Firebase
    @MockBean
    private FirebaseMessaging firebaseMessaging;

    private final String BASE_URL = "/api/images/";
    private byte[] dummyImageBytes;

    @BeforeEach
    void setUp() {
        // Dummy data untuk merepresentasikan byte gambar JPEG
        dummyImageBytes = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };
    }

    @Nested
    @DisplayName("GET /** (getImage)")
    class GetImage {

        @Test
        @DisplayName("Returns 200 OK with Image content and Cache-Control headers when file exists")
        void whenFileExists_thenReturnsImageAndCacheHeaders(@TempDir Path tempDir) throws Exception {
            // 1. Arrange: Buat file fisik sementara
            Path testFile = tempDir.resolve("test-image.jpeg");
            Files.write(testFile, dummyImageBytes);

            // Encode path aslinya ke Base64 (sesuai ekspektasi controller)
            String base64Path = Base64.getEncoder().encodeToString(testFile.toString().getBytes());

            // 2. Act & 3. Assert
            mockMvc.perform(get(BASE_URL + base64Path))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                    // CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic() akan menghasilkan header di bawah
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=86400, public"))
                    .andExpect(content().bytes(dummyImageBytes));
        }

        @Test
        @DisplayName("Returns 404 NOT FOUND when Base64 is valid but file does not exist")
        void whenFileDoesNotExist_thenReturns404(@TempDir Path tempDir) throws Exception {
            // Path yang valid secara format, tapi filenya tidak pernah dibuat
            Path ghostFile = tempDir.resolve("ghost.jpeg");
            String base64Path = Base64.getEncoder().encodeToString(ghostFile.toString().getBytes());

            mockMvc.perform(get(BASE_URL + base64Path))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Image Not Found"));
        }

        @Test
        @DisplayName("Returns 400 BAD REQUEST when URL path is not a valid Base64 string")
        void whenInvalidBase64_thenReturns400() throws Exception {
            // Mengirim string yang bukan Base64 valid (mengandung karakter terlarang atau format salah)
            String invalidBase64 = "invalid_base64_string!@#";

            mockMvc.perform(get(BASE_URL + invalidBase64))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").exists()); // Error message dari IllegalArgumentException
        }

        @Test
        @DisplayName("Returns 500 INTERNAL SERVER ERROR for unexpected exceptions")
        void whenUnexpectedException_thenReturns500() throws Exception {
            // PERBAIKAN: Menembak endpoint langsung tanpa Base64 string dan tanpa trailing slash
            // Ini akan memicu StringIndexOutOfBoundsException secara internal saat controller 
            // mengeksekusi request.getRequestURI().substring(...)
            mockMvc.perform(get("/api/images"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}