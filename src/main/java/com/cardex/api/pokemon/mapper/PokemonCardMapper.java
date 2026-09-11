package com.cardex.api.pokemon.mapper;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.response.PokemonCardSearchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PokemonCardMapper {

    @Mapping(target = "externalId", source = "id")
    @Mapping(target = "collectionName", source = "set.name")
    @Mapping(target = "cardNumber", source = "number")
    @Mapping(target = "imageUrl", source = "images.large")
    @Mapping(target = "owned", ignore = true)
    @Mapping(target = "cardId", ignore = true)
    @Mapping(target = "inWishlist", ignore = true)
    @Mapping(target = "wishlistId", ignore = true)
    @Mapping(target = "wishlistPriority", ignore = true)
    @Mapping(target = "marketPriceUsd", ignore = true)
    @Mapping(target = "marketPriceEur", ignore = true)
    PokemonCardSearchResponse toSearchResponse(
            PokemonCardApiData card
    );

    @Mapping(target = "owned", ignore = true)
    @Mapping(target = "cardId", ignore = true)
    @Mapping(target = "inWishlist", ignore = true)
    @Mapping(target = "wishlistId", ignore = true)
    @Mapping(target = "wishlistPriority", ignore = true)
    PokemonCardSearchResponse toSearchResponse(
            PokemonCardCatalogEntity card
    );
}