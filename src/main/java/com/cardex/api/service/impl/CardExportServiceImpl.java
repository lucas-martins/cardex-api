package com.cardex.api.service.impl;

import com.cardex.api.entity.CardEntity;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.service.CardExportService;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.specification.CardSpecification;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardExportServiceImpl implements CardExportService {

    private static final String CSV_HEADER =
            "External ID,Name,Collection ID,Collection,Card Number,Rarity,"
                    + "Quantity,Language,Condition,Favorite,Notes";

    private final CardRepository cardRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv() {

        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        List<CardEntity> cards = cardRepository.findAll(
                CardSpecification.userEquals(authenticatedUser),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');
        csv.append(CSV_HEADER)
                .append(System.lineSeparator());

        cards.forEach(card ->
                csv.append(toCsvLine(card))
                        .append(System.lineSeparator())
        );

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String toCsvLine(CardEntity card) {
        return String.join(",",
                escape(card.getExternalId()),
                escape(card.getName()),
                escape(card.getCollectionId()),
                escape(card.getCollectionName()),
                escape(card.getCardNumber()),
                escape(card.getRarity()),
                String.valueOf(card.getQuantity()),
                card.getLanguage().name(),
                card.getCondition().name(),
                String.valueOf(card.isFavorite()),
                escape(card.getNotes())
        );
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        String escapedValue = value.replace("\"", "\"\"");

        if (escapedValue.contains(",")
                || escapedValue.contains("\"")
                || escapedValue.contains("\n")
                || escapedValue.contains("\r")) {
            return "\"" + escapedValue + "\"";
        }

        return escapedValue;
    }
}