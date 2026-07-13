package com.cardex.api.pokemon.mapper;

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
    PokemonCardSearchResponse toSearchResponse(PokemonCardApiData card);
}