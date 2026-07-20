package com.cardex.api.exception;

public class CollectionNotFoundException extends RuntimeException {

    public CollectionNotFoundException(String collectionId) {
        super("Collection not found for ID: " + collectionId);
    }
}