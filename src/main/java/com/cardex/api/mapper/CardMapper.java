package com.cardex.api.mapper;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.entity.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CardEntity toEntity(CreateCardRequest request);

    CardResponse toResponse(CardEntity entity);
}