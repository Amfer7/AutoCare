package com.autocare.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VehicleRegistrationDto {

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull @Min(1900) @Max(2100)
    private Integer year;

    @NotBlank(message = "Color is required")
    private String color;

    @NotNull @Min(0)
    private Double currentMileage;

    private String fuelType;
    private String vinNumber;

    @NotNull(message = "Please select a maintenance policy")
    private Long policyId;
}