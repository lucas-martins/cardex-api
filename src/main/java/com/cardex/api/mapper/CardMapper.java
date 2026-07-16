package com.cardex.api.mapper;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.entity.CardEntity;
import com.cardex.api.dto.request.UpdateCardRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "collectionName", ignore = true)
    @Mapping(target = "cardNumber", ignore = true)
    @Mapping(target = "rarity", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CardEntity toEntity(CreateCardRequest request);

    CardResponse toResponse(CardEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "collectionName", ignore = true)
    @Mapping(target = "cardNumber", ignore = true)
    @Mapping(target = "rarity", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "favorite", ignore = true)
    void updateEntity(
            UpdateCardRequest request,
            @MappingTarget CardEntity cardEntity
    );
}