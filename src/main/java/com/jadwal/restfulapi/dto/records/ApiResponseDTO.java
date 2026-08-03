package com.jadwal.restfulapi.dto.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// 1. DTO Utama untuk menampung seluruh file JSON
public record ApiResponseDTO(
    @JsonProperty("dataset_info") Object datasetInfo,
    @JsonProperty("user_input") Object userInput,
    @JsonProperty("favorite_profile") String favoriteProfile,
    List<VendorDTO> vendors
) {}
