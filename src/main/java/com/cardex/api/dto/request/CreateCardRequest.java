package com.cardex.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCardRequest {

    @Size(max = 50, message = "External ID must not exceed 50 characters")
    private String externalId;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Collection name is required")
    @Size(max = 150, message = "Collection name must not exceed 150 characters")
    private String collectionName;

    @NotBlank(message = "Card number is required")
    @Size(max = 30, message = "Card number must not exceed 30 characters")
    private String cardNumber;

    @Size(max = 100, message = "Rarity must not exceed 100 characters")
    private String rarity;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Size(max = 30, message = "Language must not exceed 30 characters")
    private String language;

    @Size(max = 30, message = "Condition must not exceed 30 characters")
    private String condition;

    @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
    private String imageUrl;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}