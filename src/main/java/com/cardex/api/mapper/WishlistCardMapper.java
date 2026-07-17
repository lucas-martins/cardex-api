package com.cardex.api.mapper;

import com.cardex.api.dto.wishlist.WishlistCardResponse;
import com.cardex.api.entity.WishlistCardEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WishlistCardMapper {

    WishlistCardResponse toResponse(WishlistCardEntity entity);
}