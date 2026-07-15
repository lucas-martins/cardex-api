package com.cardex.api.pokemon.response;

import java.util.List;

public record PokemonCardSearchPageResponse(
        List<PokemonCardSearchResponse> content,
        int page,
        int pageSize,
        int count,
        int totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}