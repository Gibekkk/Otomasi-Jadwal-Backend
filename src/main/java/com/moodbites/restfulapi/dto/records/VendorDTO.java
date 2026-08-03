package com.moodbites.restfulapi.dto.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record VendorDTO(
    @JsonProperty("vendor_id") int vendorId,
    Object slots,
    List<MenuDTO> standalone,
    Object evaluation
) {}
