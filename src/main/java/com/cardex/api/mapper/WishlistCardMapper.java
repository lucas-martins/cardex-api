package com.cardex.api.mapper;

import com.cardex.api.dto.wishlist.WishlistCardResponse;
import com.cardex.api.entity.WishlistCardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistCardMapper {

    @Mapping(target = "marketPriceUsd", ignore = true)
    @Mapping(target = "marketPriceEur", ignore = true)
    @Mapping(target = "marketPriceBrl", ignore = true)
    WishlistCardResponse toResponse(WishlistCardEntity entity);
}