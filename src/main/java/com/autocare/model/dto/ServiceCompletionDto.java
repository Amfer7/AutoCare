package com.autocare.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceCompletionDto {
    @NotBlank(message = "Work performed description is required")
    private String workPerformed;

    @NotNull
    @Min(0)
    private Double mileageAtService;

    private String partsReplaced;
    private String technicianNotes;

    @NotNull
    @Min(0)
    private Double laborCost;

    @NotNull
    @Min(0)
    private Double partsCost;

    private String billNotes;
}
