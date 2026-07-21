package com.cardex.api.service.impl;

import com.cardex.api.component.cardimport.CardCsvReader;
import com.cardex.api.component.cardimport.CardImportValidator;
import com.cardex.api.dto.response.CardImportPreviewItemResponse;
import com.cardex.api.dto.response.CardImportPreviewResponse;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.service.CardImportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cardex.api.component.cardimport.CardCsvHeaders;
import com.cardex.api.dto.response.CardImportResponse;
import com.cardex.api.entity.CardEntity;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.repository.CardRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CardImportServiceImpl implements CardImportService {

    private final CardCsvReader cardCsvReader;
    private final CardImportValidator cardImportValidator;
    private final CardRepository cardRepository;
    private final PokemonTcgClient pokemonTcgClient;

    @Override
    public CardImportPreviewResponse previewCsv(MultipartFile file) {
        List<CSVRecord> records = cardCsvReader.read(file);

        List<CardImportPreviewItemResponse> items = records.stream()
                .map(cardImportValidator::validate)
                .toList();

        long validRows = items.stream()
                .filter(CardImportPreviewItemResponse::valid)
                .count();

        long invalidRows = items.size() - validRows;

        return new CardImportPreviewResponse(
                items.size(),
                validRows,
                invalidRows,
                items
        );
    }

    @Override
    @Transactional
    public CardImportResponse importCsv(MultipartFile file) {
        List<CSVRecord> records = cardCsvReader.read(file);

        List<CardEntity> cardsToImport = new ArrayList<>();
        List<CardImportPreviewItemResponse> skippedItems = new ArrayList<>();
        Set<String> importKeys = new HashSet<>();

        for (CSVRecord record : records) {
            CardImportPreviewItemResponse validation =
                    cardImportValidator.validate(record);

            if (!validation.valid()) {
                skippedItems.add(validation);
                continue;
            }

            String importKey = buildImportKey(record);

            if (!importKeys.add(importKey)) {
                skippedItems.add(new CardImportPreviewItemResponse(
                        record.getRecordNumber() + 1,
                        record.get(CardCsvHeaders.EXTERNAL_ID),
                        record.get(CardCsvHeaders.NAME),
                        record.get(CardCsvHeaders.COLLECTION),
                        record.get(CardCsvHeaders.CARD_NUMBER),
                        parseQuantitySafely(record),
                        false,
                        "Duplicated card in the CSV file."
                ));

                continue;
            }

            try {
                cardsToImport.add(toEntity(record));
            } catch (Exception exception) {
                skippedItems.add(new CardImportPreviewItemResponse(
                        record.getRecordNumber() + 1,
                        record.get(CardCsvHeaders.EXTERNAL_ID),
                        record.get(CardCsvHeaders.NAME),
                        record.get(CardCsvHeaders.COLLECTION),
                        record.get(CardCsvHeaders.CARD_NUMBER),
                        parseQuantitySafely(record),
                        false,
                        "Could not retrieve the card metadata."
                ));
            }
        }

        cardRepository.saveAll(cardsToImport);

        return new CardImportResponse(
                records.size(),
                cardsToImport.size(),
                skippedItems.size(),
                skippedItems
        );
    }

    private CardEntity toEntity(CSVRecord record) {
        String externalId = record.get(CardCsvHeaders.EXTERNAL_ID);

        PokemonCardApiSingleResponse response =
                pokemonTcgClient.findById(externalId);

        PokemonCardApiData pokemonCard = response.data();

        if (pokemonCard == null || pokemonCard.set() == null) {
            throw new IllegalArgumentException(
                    "Card metadata was not found."
            );
        }

        CardEntity entity = new CardEntity();

        entity.setExternalId(externalId);
        entity.setName(pokemonCard.name());
        entity.setCollectionId(pokemonCard.set().id());
        entity.setCollectionName(pokemonCard.set().name());
        entity.setCollectionTotal(pokemonCard.set().total());
        entity.setCardNumber(pokemonCard.number());
        entity.setRarity(pokemonCard.rarity());
        entity.setQuantity(
                Integer.parseInt(record.get(CardCsvHeaders.QUANTITY))
        );
        entity.setLanguage(
                CardLanguage.valueOf(
                        record.get(CardCsvHeaders.LANGUAGE)
                )
        );
        entity.setCondition(
                CardCondition.valueOf(
                        record.get(CardCsvHeaders.CONDITION)
                )
        );
        entity.setFavorite(
                Boolean.parseBoolean(
                        record.get(CardCsvHeaders.FAVORITE)
                )
        );
        entity.setNotes(
                emptyToNull(record.get(CardCsvHeaders.NOTES))
        );

        if (pokemonCard.images() != null) {
            entity.setImageUrl(pokemonCard.images().large());
        }

        return entity;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value;
    }

    private int parseQuantitySafely(CSVRecord record) {
        try {
            return Integer.parseInt(
                    record.get(CardCsvHeaders.QUANTITY)
            );
        } catch (Exception exception) {
            return 0;
        }
    }

    private String buildImportKey(CSVRecord record) {
        return String.join(
                "|",
                record.get(CardCsvHeaders.EXTERNAL_ID),
                record.get(CardCsvHeaders.LANGUAGE),
                record.get(CardCsvHeaders.CONDITION)
        );
    }
}