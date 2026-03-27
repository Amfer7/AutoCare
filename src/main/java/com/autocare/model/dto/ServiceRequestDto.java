package com.autocare.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceRequestDto {
    @NotNull(message = "Vehicle must be selected")
    private Long vehicleId;

    @NotBlank(message = "Please describe the service needed")
    private String description;
}