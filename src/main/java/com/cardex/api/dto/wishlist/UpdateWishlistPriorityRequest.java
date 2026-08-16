package com.cardex.api.dto.wishlist;

import com.cardex.api.enumeration.WishlistPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateWishlistPriorityRequest(

        @NotNull(message = "Priority is required")
        WishlistPriority priority

) {
}