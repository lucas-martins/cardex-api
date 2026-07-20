package com.cardex.api.repository.projection;

public interface CollectionProgressProjection {

    String getCollectionId();

    String getCollectionName();

    Integer getCollectionTotal();

    Long getOwnedCards();
}