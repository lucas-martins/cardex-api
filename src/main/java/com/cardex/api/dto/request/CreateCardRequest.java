package com.cardex.api.dto.request;

import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCardRequest {

    @NotBlank(message = "External ID is required")
    @Size(max = 50, message = "External ID must not exceed 50 characters")
    private String externalId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Language is required")
    private CardLanguage language;

    @NotNull(message = "Condition is required")
    private CardCondition condition;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}