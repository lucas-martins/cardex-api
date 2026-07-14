package com.cardex.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCardRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Size(max = 30, message = "Language must not exceed 30 characters")
    private String language;

    @Size(max = 30, message = "Condition must not exceed 30 characters")
    private String condition;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}