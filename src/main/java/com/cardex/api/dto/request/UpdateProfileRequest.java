package com.cardex.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "Name is required.")
        @Size(
                max = 100,
                message = "Name must have at most 100 characters."
        )
        String name
) {
}