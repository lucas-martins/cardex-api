package com.cardex.api.dto.wishlist;

import com.cardex.api.enumeration.WishlistPriority;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateWishlistDetailsRequest(
        WishlistPriority priority,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes,

        @Size(max = 1000, message = "Store URL must not exceed 1000 characters")
        String storeUrl,

        @DecimalMin(value = "0.0", inclusive = true, message = "Target price must be positive")
        BigDecimal targetPriceUsd
) {
}
