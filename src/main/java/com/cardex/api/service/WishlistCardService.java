package com.cardex.api.service;

import com.cardex.api.dto.wishlist.UpdateWishlistDetailsRequest;
import com.cardex.api.dto.wishlist.UpdateWishlistPriorityRequest;
import com.cardex.api.dto.wishlist.WishlistCardRequest;
import com.cardex.api.dto.wishlist.WishlistCardResponse;

import java.util.List;

public interface WishlistCardService {

    WishlistCardResponse create(WishlistCardRequest request);

    List<WishlistCardResponse> findAll();

    void delete(Long id);

    WishlistCardResponse updatePriority(
            Long id,
            UpdateWishlistPriorityRequest request
    );

    WishlistCardResponse updateDetails(
            Long id,
            UpdateWishlistDetailsRequest request
    );
}