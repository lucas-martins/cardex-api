package com.cardex.api.dto.wishlist;

import com.cardex.api.enumeration.WishlistPriority;
import jakarta.validation.constraints.NotBlank;

public record WishlistCardRequest(

        @NotBlank(message = "External ID is required")
        String externalId,

        WishlistPriority priority

) {
}