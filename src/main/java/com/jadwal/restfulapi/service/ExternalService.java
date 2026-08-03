// package com.jadwal.restfulapi.service;

// import java.io.IOException;
// import java.net.ConnectException;
// import java.net.URI;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
// import java.net.http.HttpTimeoutException;
// import java.time.Duration;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.DeserializationFeature;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.jadwal.restfulapi.dto.records.ApiResponseDTO;
// import com.jadwal.restfulapi.dto.records.MenuDTO;

// import org.springframework.stereotype.Service;

// @Service
// public class ExternalService {

//     private final String API_ORDER_HERE = "http://103.185.52.14:8067";
//     private final ObjectMapper mapper;

//     public ExternalService() {
//         this.mapper = new ObjectMapper();
//         // Supaya tidak error jika ada field JSON yang tidak kita daftarkan di DTO
//         this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//     }

//     public List<String> getRecommendations(String userId, String mood) throws JsonProcessingException {
//         // Parsing Map to JSON body
//         HttpClient client = HttpClient.newBuilder()
//                 .connectTimeout(Duration.ofSeconds(5))
//                 .build();
//         // Build POST request with no body
//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(API_ORDER_HERE + "/recommend-external/" + mood + "/" + userId))
//                 .header("Content-Type", "application/json")
//                 .timeout(Duration.ofSeconds(10))
//                 .build();

//         try {
//             HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//             ApiResponseDTO mappedResult = mapper.readValue(response.body(), ApiResponseDTO.class);

//             // Setelah menjadi objek Java, kamu bebas mengolahnya.
//             // Contoh di bawah ini mengambil semua "Nama Menu" dari setiap vendor:
//             if (mappedResult != null && mappedResult.vendors() != null) {
//                 return mappedResult.vendors().stream() // Loop semua vendor
//                         .flatMap(vendor -> vendor.standalone().stream()) // Masuk ke list standalone menu
//                         .map(MenuDTO::namaMenu) // Ambil properti namaMenu saja
//                         .collect(Collectors.toList()); // Jadikan List<String>
//             } else {
//                 throw new IllegalArgumentException("False JSON Response.");
//             }
//         } catch (HttpTimeoutException e) {
//             // Error spesifik jika melebihi batas waktu (Read Timeout)
//             System.err.println("Order Here Server Timed Out.");
//         } catch (ConnectException e) {
//             // Error spesifik jika server mati atau tidak bisa disambung (Connect Timeout)
//             System.err.println("Failed To Connect To Order Here Servers.");
//         } catch (IOException | InterruptedException e) {
//             e.printStackTrace();
//         }
//         return List.of();
//     }

//     // public List<String> getRecommendations(Map<String, Object> preferenceData) throws JsonProcessingException {
//     //     // Parsing Map to JSON body
//     //     String jsonBody = mapper.writeValueAsString(preferenceData);
//     //     HttpClient client = HttpClient.newBuilder()
//     //             .connectTimeout(Duration.ofSeconds(5))
//     //             .build();
//     //     // Build POST request with no body
//     //     HttpRequest request = HttpRequest.newBuilder()
//     //             .uri(URI.create(API_ORDER_HERE + "/recommend"))
//     //             .header("Content-Type", "application/json")
//     //             .timeout(Duration.ofSeconds(10))
//     //             .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
//     //             .build();

//     //     try {
//     //         HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//     //         ApiResponseDTO mappedResult = mapper.readValue(response.body(), ApiResponseDTO.class);

//     //         // Setelah menjadi objek Java, kamu bebas mengolahnya.
//     //         // Contoh di bawah ini mengambil semua "Nama Menu" dari setiap vendor:
//     //         if (mappedResult != null && mappedResult.vendors() != null) {
//     //             return mappedResult.vendors().stream() // Loop semua vendor
//     //                     .flatMap(vendor -> vendor.standalone().stream()) // Masuk ke list standalone menu
//     //                     .map(MenuDTO::namaMenu) // Ambil properti namaMenu saja
//     //                     .collect(Collectors.toList()); // Jadikan List<String>
//     //         } else {
//     //             throw new IllegalArgumentException("False JSON Response.");
//     //         }
//     //     } catch (HttpTimeoutException e) {
//     //         // Error spesifik jika melebihi batas waktu (Read Timeout)
//     //         System.err.println("Order Here Server Timed Out.");
//     //     } catch (ConnectException e) {
//     //         // Error spesifik jika server mati atau tidak bisa disambung (Connect Timeout)
//     //         System.err.println("Failed To Connect To Order Here Servers.");
//     //     } catch (IOException | InterruptedException e) {
//     //         e.printStackTrace();
//     //     }
//     //     return List.of();
//     // }
// }
