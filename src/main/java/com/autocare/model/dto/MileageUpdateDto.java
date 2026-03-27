package com.autocare.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MileageUpdateDto {
    @NotNull(message = "New mileage is required")
    @Min(value = 0)
    private Double newMileage;
    private String notes;
}