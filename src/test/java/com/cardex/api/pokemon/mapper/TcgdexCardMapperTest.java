package com.cardex.api.pokemon.mapper;

import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardCount;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardDetail;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardmarketPricing;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexPricing;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexSerieBrief;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexSetBrief;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexTcgPlayerPricing;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexVariantPrice;
import com.cardex.api.pokemon.PokemonCardPriceExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TcgdexCardMapperTest {

    private final TcgdexCardMapper mapper = new TcgdexCardMapper();

    @Test
    void shouldMapTcgdexCardDetailToInternalDto() {
        TcgdexCardDetail detail = new TcgdexCardDetail(
                "sm1-12",
                12,
                "Decidueye GX",
                "Ultra Rare",
                "https://assets.tcgdex.net/en/sm/sm1/12",
                new TcgdexSetBrief(
                        "sm1",
                        "Sun & Moon",
                        new TcgdexCardCount(149, 172),
                        new TcgdexSerieBrief("sm", "Sun & Moon"),
                        new com.cardex.api.pokemon.dto.tcgdex.TcgdexSetAbbreviation(
                                "SUM"
                        ),
                        "SUM"
                ),
                new TcgdexPricing(
                        new TcgdexTcgPlayerPricing(
                                "USD",
                                "2026-11-10",
                                new TcgdexVariantPrice(
                                        new BigDecimal("0.10"),
                                        new BigDecimal("0.20"),
                                        new BigDecimal("1.00"),
                                        new BigDecimal("0.18"),
                                        null
                                ),
                                null,
                                new TcgdexVariantPrice(
                                        new BigDecimal("0.50"),
                                        new BigDecimal("0.80"),
                                        new BigDecimal("2.00"),
                                        new BigDecimal("0.75"),
                                        null
                                ),
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        new TcgdexCardmarketPricing(
                                "EUR",
                                "2026-11-10",
                                new BigDecimal("0.90"),
                                new BigDecimal("0.40"),
                                new BigDecimal("1.15")
                        )
                )
        );

        PokemonCardApiData card = mapper.toCardApiData(detail);

        assertEquals("sm1-12", card.id());
        assertEquals("12", card.number());
        assertEquals("Decidueye GX", card.name());
        assertEquals("sm1", card.set().id());
        assertEquals("Sun & Moon", card.set().series());
        assertEquals(149, card.set().printedTotal());
        assertEquals(172, card.set().total());
        assertEquals(
                "https://assets.tcgdex.net/en/sm/sm1/12/high.png",
                card.images().large()
        );
        assertNotNull(card.tcgplayer());
        assertNotNull(card.tcgplayer().prices().get("reverseHolofoil"));
        assertEquals(
                new BigDecimal("0.18"),
                PokemonCardPriceExtractor.usdFrom(card)
        );
        assertEquals(
                new BigDecimal("1.15"),
                PokemonCardPriceExtractor.eurFrom(card)
        );
    }
}
