package com.cardex.api.exception;

public class WishlistCardNotFoundException extends RuntimeException {

    public WishlistCardNotFoundException(Long id) {
        super("Wishlist card not found for ID: " + id);
    }
}