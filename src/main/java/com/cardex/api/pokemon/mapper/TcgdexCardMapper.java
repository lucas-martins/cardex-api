package com.cardex.api.pokemon.mapper;

import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardImagesApiData;
import com.cardex.api.pokemon.dto.PokemonCardmarketApiData;
import com.cardex.api.pokemon.dto.PokemonCardmarketPricesApiData;
import com.cardex.api.pokemon.dto.PokemonSetApiData;
import com.cardex.api.pokemon.dto.PokemonTcgPlayerApiData;
import com.cardex.api.pokemon.dto.PokemonTcgPlayerPriceApiData;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardBrief;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardCount;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardDetail;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardmarketPricing;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexSerieBrief;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexSetBrief;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexTcgPlayerPricing;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexVariantPrice;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TcgdexCardMapper {

    private static final String IMAGE_LARGE_SUFFIX = "/high.png";
    private static final String IMAGE_SMALL_SUFFIX = "/low.webp";

    public PokemonCardApiData toCardApiData(TcgdexCardDetail card) {
        if (card == null) {
            return null;
        }

        return new PokemonCardApiData(
                card.id(),
                card.name(),
                toLocalId(card.localId()),
                card.rarity(),
                toSetApiData(card.set()),
                toImages(card.image()),
                toTcgPlayer(card),
                toCardmarket(card)
        );
    }

    public PokemonCardApiData toCardApiData(
            TcgdexCardBrief brief,
            TcgdexSetDetailContext setContext
    ) {
        if (brief == null) {
            return null;
        }

        return new PokemonCardApiData(
                brief.id(),
                brief.name(),
                toLocalId(brief.localId()),
                null,
                setContext == null ? null : setContext.toSetApiData(),
                toImages(brief.image()),
                null,
                null
        );
    }

    private String toLocalId(Object localId) {
        if (localId == null) {
            return null;
        }

        return String.valueOf(localId);
    }

    public PokemonSetApiData toSetApiData(TcgdexSetBrief set) {
        if (set == null) {
            return null;
        }

        TcgdexCardCount cardCount = set.cardCount();
        TcgdexSerieBrief serie = set.serie();

        return new PokemonSetApiData(
                set.id(),
                set.name(),
                serie != null ? serie.name() : null,
                cardCount != null ? cardCount.official() : null,
                cardCount != null ? cardCount.total() : null,
                resolveAbbreviation(set)
        );
    }

    public PokemonSetApiData toSetApiData(
            TcgdexSetBrief set,
            String seriesName
    ) {
        if (set == null) {
            return null;
        }

        TcgdexCardCount cardCount = set.cardCount();

        return new PokemonSetApiData(
                set.id(),
                set.name(),
                seriesName,
                cardCount != null ? cardCount.official() : null,
                cardCount != null ? cardCount.total() : null,
                resolveAbbreviation(set)
        );
    }

    private String resolveAbbreviation(TcgdexSetBrief set) {
        if (set.abbreviation() != null
                && set.abbreviation().official() != null
                && !set.abbreviation().official().isBlank()) {
            return set.abbreviation().official().trim().toUpperCase();
        }

        if (set.tcgOnline() != null && !set.tcgOnline().isBlank()) {
            return set.tcgOnline().trim().toUpperCase();
        }

        return null;
    }

    private PokemonCardImagesApiData toImages(String imageBaseUrl) {
        if (imageBaseUrl == null || imageBaseUrl.isBlank()) {
            return null;
        }

        String normalized = imageBaseUrl.endsWith("/")
                ? imageBaseUrl.substring(0, imageBaseUrl.length() - 1)
                : imageBaseUrl;

        return new PokemonCardImagesApiData(
                normalized + IMAGE_SMALL_SUFFIX,
                normalized + IMAGE_LARGE_SUFFIX
        );
    }

    private PokemonTcgPlayerApiData toTcgPlayer(TcgdexCardDetail card) {
        if (card.pricing() == null || card.pricing().tcgplayer() == null) {
            return null;
        }

        TcgdexTcgPlayerPricing pricing = card.pricing().tcgplayer();
        Map<String, PokemonTcgPlayerPriceApiData> prices = new LinkedHashMap<>();

        putPrice(prices, "normal", pricing.normal());
        putPrice(prices, "holofoil", pricing.holofoil());
        putPrice(prices, "reverseHolofoil", firstNonNull(
                pricing.reverseHolofoil(),
                pricing.reverse()
        ));
        putPrice(prices, "1stEdition", pricing.firstEdition());
        putPrice(prices, "1stEditionHolofoil", pricing.firstEditionHolofoil());
        putPrice(prices, "unlimited", pricing.unlimited());
        putPrice(prices, "unlimitedHolofoil", pricing.unlimitedHolofoil());

        if (prices.isEmpty()) {
            return null;
        }

        return new PokemonTcgPlayerApiData(
                null,
                pricing.updated(),
                prices
        );
    }

    private PokemonCardmarketApiData toCardmarket(TcgdexCardDetail card) {
        if (card.pricing() == null || card.pricing().cardmarket() == null) {
            return null;
        }

        TcgdexCardmarketPricing pricing = card.pricing().cardmarket();

        return new PokemonCardmarketApiData(
                null,
                pricing.updated(),
                new PokemonCardmarketPricesApiData(
                        pricing.avg(),
                        pricing.low(),
                        pricing.trend()
                )
        );
    }

    private void putPrice(
            Map<String, PokemonTcgPlayerPriceApiData> prices,
            String finish,
            TcgdexVariantPrice price
    ) {
        if (price == null) {
            return;
        }

        prices.put(
                finish,
                new PokemonTcgPlayerPriceApiData(
                        price.lowPrice(),
                        price.midPrice(),
                        price.highPrice(),
                        price.marketPrice(),
                        price.directLowPrice()
                )
        );
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public record TcgdexSetDetailContext(
            String id,
            String name,
            String seriesName,
            Integer printedTotal,
            Integer total
    ) {
        public PokemonSetApiData toSetApiData() {
            return new PokemonSetApiData(
                    id,
                    name,
                    seriesName,
                    printedTotal,
                    total
            );
        }

        public static TcgdexSetDetailContext from(
                TcgdexSetBrief set,
                String seriesName
        ) {
            if (set == null) {
                return null;
            }

            TcgdexCardCount cardCount = set.cardCount();

            return new TcgdexSetDetailContext(
                    set.id(),
                    set.name(),
                    seriesName,
                    cardCount != null ? cardCount.official() : null,
                    cardCount != null ? cardCount.total() : null
            );
        }
    }
}
