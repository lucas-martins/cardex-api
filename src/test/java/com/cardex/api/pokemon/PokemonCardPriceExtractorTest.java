package com.cardex.api.pokemon;

import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardmarketApiData;
import com.cardex.api.pokemon.dto.PokemonCardmarketPricesApiData;
import com.cardex.api.pokemon.dto.PokemonTcgPlayerApiData;
import com.cardex.api.pokemon.dto.PokemonTcgPlayerPriceApiData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PokemonCardPriceExtractorTest {

    @Test
    void shouldPreferNormalMarketPriceInUsd() {
        Map<String, PokemonTcgPlayerPriceApiData> prices =
                new LinkedHashMap<>();

        prices.put(
                "holofoil",
                new PokemonTcgPlayerPriceApiData(
                        new BigDecimal("2.00"),
                        new BigDecimal("3.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("4.50"),
                        null
                )
        );

        prices.put(
                "normal",
                new PokemonTcgPlayerPriceApiData(
                        new BigDecimal("0.10"),
                        new BigDecimal("0.20"),
                        new BigDecimal("0.50"),
                        new BigDecimal("0.18"),
                        null
                )
        );

        PokemonCardApiData card =
                cardWithPrices(prices, null);

        assertEquals(
                new BigDecimal("0.18"),
                PokemonCardPriceExtractor.usdFrom(card)
        );
    }

    @Test
    void shouldFallbackToMidPriceWhenMarketIsMissing() {
        Map<String, PokemonTcgPlayerPriceApiData> prices =
                new LinkedHashMap<>();

        prices.put(
                "holofoil",
                new PokemonTcgPlayerPriceApiData(
                        new BigDecimal("1.10"),
                        new BigDecimal("2.25"),
                        new BigDecimal("4.00"),
                        null,
                        null
                )
        );

        PokemonCardApiData card =
                cardWithPrices(prices, null);

        assertEquals(
                new BigDecimal("2.25"),
                PokemonCardPriceExtractor.usdFrom(card)
        );
    }

    @Test
    void shouldUseCardmarketTrendPriceInEur() {
        PokemonCardmarketPricesApiData prices =
                new PokemonCardmarketPricesApiData(
                        new BigDecimal("0.90"),
                        new BigDecimal("0.40"),
                        new BigDecimal("1.15")
                );

        PokemonCardApiData card =
                cardWithPrices(
                        Map.of(),
                        new PokemonCardmarketApiData(
                                "https://cardmarket.example",
                                "2026/09/11",
                                prices
                        )
                );

        assertEquals(
                new BigDecimal("1.15"),
                PokemonCardPriceExtractor.eurFrom(card)
        );
    }

    @Test
    void shouldReturnNullWhenNoPriceIsAvailable() {
        PokemonCardApiData card =
                cardWithPrices(Map.of(), null);

        assertNull(PokemonCardPriceExtractor.usdFrom(card));
        assertNull(PokemonCardPriceExtractor.eurFrom(card));
    }

    @Test
    void shouldMultiplyUnitPriceByQuantity() {
        assertEquals(
                new BigDecimal("3.50"),
                PokemonCardPriceExtractor.multiply(
                        new BigDecimal("1.75"),
                        2
                )
        );

        assertNull(
                PokemonCardPriceExtractor.multiply(
                        null,
                        2
                )
        );
    }

    private PokemonCardApiData cardWithPrices(
            Map<String, PokemonTcgPlayerPriceApiData> tcgplayerPrices,
            PokemonCardmarketApiData cardmarket
    ) {
        return new PokemonCardApiData(
                "sm1-1",
                "Caterpie",
                "1",
                "Common",
                null,
                null,
                tcgplayerPrices.isEmpty()
                        ? null
                        : new PokemonTcgPlayerApiData(
                                "https://tcgplayer.example",
                                "2026/09/11",
                                tcgplayerPrices
                        ),
                cardmarket
        );
    }
}
