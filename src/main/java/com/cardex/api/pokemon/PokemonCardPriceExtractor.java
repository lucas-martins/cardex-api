package com.cardex.api.pokemon;

import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.dto.wishlist.WishlistCardResponse;
import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardmarketPricesApiData;
import com.cardex.api.pokemon.dto.PokemonTcgPlayerPriceApiData;
import com.cardex.api.pokemon.response.PokemonCardSearchResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public final class PokemonCardPriceExtractor {

    private static final int PRICE_SCALE = 2;

    private static final List<String> FINISH_ORDER = List.of(
            "normal",
            "holofoil",
            "unlimitedHolofoil",
            "unlimited",
            "reverseHolofoil",
            "1stEditionHolofoil",
            "1stEdition"
    );

    private PokemonCardPriceExtractor() {
    }

    public static BigDecimal usdFrom(PokemonCardApiData card) {
        if (card == null
                || card.tcgplayer() == null
                || card.tcgplayer().prices() == null
                || card.tcgplayer().prices().isEmpty()) {
            return null;
        }

        Map<String, PokemonTcgPlayerPriceApiData> prices =
                card.tcgplayer().prices();

        for (String finish : FINISH_ORDER) {
            BigDecimal value =
                    pickUsd(
                            prices.get(finish)
                    );

            if (value != null) {
                return value;
            }
        }

        for (Map.Entry<String, PokemonTcgPlayerPriceApiData> entry
                : prices.entrySet()) {
            if (FINISH_ORDER.contains(entry.getKey())) {
                continue;
            }

            BigDecimal value =
                    pickUsd(
                            entry.getValue()
                    );

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public static BigDecimal eurFrom(PokemonCardApiData card) {
        if (card == null
                || card.cardmarket() == null
                || card.cardmarket().prices() == null) {
            return null;
        }

        PokemonCardmarketPricesApiData prices =
                card.cardmarket().prices();

        if (prices.trendPrice() != null) {
            return scale(prices.trendPrice());
        }

        if (prices.averageSellPrice() != null) {
            return scale(prices.averageSellPrice());
        }

        if (prices.lowPrice() != null) {
            return scale(prices.lowPrice());
        }

        return null;
    }

    public static void applyToCatalog(
            PokemonCardCatalogEntity entity,
            PokemonCardApiData card
    ) {
        if (entity == null) {
            return;
        }

        entity.setMarketPriceUsd(usdFrom(card));
        entity.setMarketPriceEur(eurFrom(card));
        entity.setPriceUpdatedAt(LocalDateTime.now());
    }

    public static CardResponse enrich(
            CardResponse response,
            PokemonCardCatalogEntity catalog,
            Integer quantity,
            BiFunction<BigDecimal, BigDecimal, BigDecimal> toBrl
    ) {
        if (response == null || catalog == null) {
            return response;
        }

        BigDecimal marketPriceUsd = catalog.getMarketPriceUsd();
        BigDecimal marketPriceEur = catalog.getMarketPriceEur();
        BigDecimal marketPriceBrl =
                toBrl != null
                        ? toBrl.apply(marketPriceUsd, marketPriceEur)
                        : null;

        return response.toBuilder()
                .marketPriceUsd(marketPriceUsd)
                .marketPriceEur(marketPriceEur)
                .marketPriceBrl(marketPriceBrl)
                .estimatedValueUsd(
                        multiply(marketPriceUsd, quantity)
                )
                .estimatedValueEur(
                        multiply(marketPriceEur, quantity)
                )
                .estimatedValueBrl(
                        multiply(marketPriceBrl, quantity)
                )
                .build();
    }

    public static WishlistCardResponse enrich(
            WishlistCardResponse response,
            PokemonCardCatalogEntity catalog,
            BiFunction<BigDecimal, BigDecimal, BigDecimal> toBrl
    ) {
        if (response == null || catalog == null) {
            return response;
        }

        BigDecimal marketPriceUsd = catalog.getMarketPriceUsd();
        BigDecimal marketPriceEur = catalog.getMarketPriceEur();

        return new WishlistCardResponse(
                response.id(),
                response.externalId(),
                response.name(),
                response.cardNumber(),
                response.collectionId(),
                response.collectionName(),
                response.series(),
                response.rarity(),
                response.imageUrl(),
                response.priority(),
                response.createdAt(),
                response.updatedAt(),
                marketPriceUsd,
                marketPriceEur,
                toBrl != null
                        ? toBrl.apply(marketPriceUsd, marketPriceEur)
                        : null
        );
    }

    public static PokemonCardSearchResponse enrich(
            PokemonCardSearchResponse response,
            BigDecimal marketPriceUsd,
            BigDecimal marketPriceEur,
            BigDecimal marketPriceBrl
    ) {
        if (response == null) {
            return null;
        }

        return new PokemonCardSearchResponse(
                response.externalId(),
                response.name(),
                response.collectionName(),
                response.cardNumber(),
                response.rarity(),
                response.imageUrl(),
                response.owned(),
                response.cardId(),
                response.inWishlist(),
                response.wishlistId(),
                response.wishlistPriority(),
                marketPriceUsd,
                marketPriceEur,
                marketPriceBrl
        );
    }

    public static BigDecimal multiply(
            BigDecimal unitPrice,
            Integer quantity
    ) {
        if (unitPrice == null || quantity == null) {
            return null;
        }

        return unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal pickUsd(
            PokemonTcgPlayerPriceApiData price
    ) {
        if (price == null) {
            return null;
        }

        if (price.market() != null) {
            return scale(price.market());
        }

        if (price.mid() != null) {
            return scale(price.mid());
        }

        if (price.low() != null) {
            return scale(price.low());
        }

        return null;
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }
}
