package com.cardex.api.exception;

public class WishlistCardAlreadyExistsException extends RuntimeException {

    public WishlistCardAlreadyExistsException(String externalId) {
        super("Card already exists in wishlist: " + externalId);
    }
}