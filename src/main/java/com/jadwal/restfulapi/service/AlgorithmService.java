package com.jadwal.restfulapi.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Fire-and-forget caller ke startGenerate service (Python, local only,
 * port 8082). Method di sini @Async supaya thread yang menangani request
 * dari frontend (TimelineController#generate) langsung return, TIDAK
 * menunggu Python selesai (Python bisa sleep 10 detik+).
 *
 * Hasil generate sesungguhnya dikirim balik oleh Python lewat
 * POST /timeline/generateComplete, bukan lewat return value method ini.
 */
@Service
public class AlgorithmService {

    private final String startGenerateUrl = "http://startgenerate-service:8082/startGenerate";

    private final RestTemplate restTemplate = new RestTemplate();

    @Async("asyncExecutor")
    public CompletableFuture<Void> triggerStartGenerate(String secretKey) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                    Map.of("secretKey", secretKey), headers);

            restTemplate.postForEntity(startGenerateUrl, entity, String.class);
        } catch (Exception e) {
            // Fire-and-forget: gagal connect/timeout ke Python cukup di-log, tidak
            // dilempar ke caller karena response ke frontend sudah terkirim duluan.
            System.err.println("Failed To Fetch Algorithm (Python): " + e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}