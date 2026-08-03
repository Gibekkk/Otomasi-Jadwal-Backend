package com.moodbites.restfulapi.dto.records;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MenuDTO(
    @JsonProperty("Nama Menu") String namaMenu,
    @JsonProperty("base_distance") double baseDistance,
    @JsonProperty("similarity_bonus") double similarityBonus,
    @JsonProperty("final_score") double finalScore,
    @JsonProperty("match_pct") double matchPct
) {}