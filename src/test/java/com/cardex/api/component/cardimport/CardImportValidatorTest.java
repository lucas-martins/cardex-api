package com.cardex.api.component.cardimport;

import com.cardex.api.dto.response.CardImportPreviewItemResponse;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.repository.CardRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardImportValidatorTest {

    @Mock
    private CardRepository cardRepository;

    private CardImportValidator cardImportValidator;

    @BeforeEach
    void setUp() {
        cardImportValidator = new CardImportValidator(cardRepository);
    }

    @Test
    void shouldValidateValidRecord() throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "Decidueye-GX",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "2",
                "ENGLISH",
                "NEAR_MINT",
                "true",
                "Imported card"
        );

        when(cardRepository.existsByExternalIdAndLanguageAndCondition(
                "sm1-12",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        )).thenReturn(false);

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertTrue(result.valid());
        assertNull(result.error());
        assertEquals("sm1-12", result.externalId());
        assertEquals(2, result.quantity());

        verify(cardRepository)
                .existsByExternalIdAndLanguageAndCondition(
                        "sm1-12",
                        CardLanguage.ENGLISH,
                        CardCondition.NEAR_MINT
                );
    }

    @Test
    void shouldInvalidateRecordWhenExternalIdIsBlank()
            throws IOException {
        CSVRecord record = createRecord(
                "",
                "Decidueye-GX",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "1",
                "ENGLISH",
                "NEAR_MINT",
                "false",
                ""
        );

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertEquals(
                "External ID is required.",
                result.error()
        );

        verify(cardRepository, never())
                .existsByExternalIdAndLanguageAndCondition(
                        "",
                        CardLanguage.ENGLISH,
                        CardCondition.NEAR_MINT
                );
    }

    @Test
    void shouldInvalidateRecordWhenNameIsBlank()
            throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "1",
                "ENGLISH",
                "NEAR_MINT",
                "false",
                ""
        );

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertEquals(
                "Name is required.",
                result.error()
        );
    }

    @Test
    void shouldInvalidateRecordWhenCollectionIdIsBlank()
            throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "Decidueye-GX",
                "",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "1",
                "ENGLISH",
                "NEAR_MINT",
                "false",
                ""
        );

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertEquals(
                "Collection ID is required.",
                result.error()
        );
    }

    @Test
    void shouldInvalidateRecordWhenQuantityIsZero()
            throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "Decidueye-GX",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "0",
                "ENGLISH",
                "NEAR_MINT",
                "false",
                ""
        );

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertEquals(0, result.quantity());
        assertEquals(
                "Quantity must be greater than zero.",
                result.error()
        );
    }

    @Test
    void shouldInvalidateRecordWhenQuantityIsNotNumeric()
            throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "Decidueye-GX",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "invalid",
                "ENGLISH",
                "NEAR_MINT",
                "false",
                ""
        );

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertEquals(0, result.quantity());
        assertEquals(
                "For input string: \"invalid\"",
                result.error()
        );
    }

    @Test
    void shouldInvalidateRecordWhenLanguageIsInvalid()
            throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "Decidueye-GX",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "1",
                "KLINGON",
                "NEAR_MINT",
                "false",
                ""
        );

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertTrue(result.error().contains("KLINGON"));
    }

    @Test
    void shouldInvalidateRecordWhenConditionIsInvalid()
            throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "Decidueye-GX",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "1",
                "ENGLISH",
                "PERFECT",
                "false",
                ""
        );

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertTrue(result.error().contains("PERFECT"));
    }

    @Test
    void shouldInvalidateRecordWhenFavoriteIsInvalid()
            throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "Decidueye-GX",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "1",
                "ENGLISH",
                "NEAR_MINT",
                "yes",
                ""
        );

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertEquals(
                "Favorite must be true or false.",
                result.error()
        );
    }

    @Test
    void shouldInvalidateRecordWhenCardAlreadyExists()
            throws IOException {
        CSVRecord record = createRecord(
                "sm1-12",
                "Decidueye-GX",
                "sm1",
                "Sun & Moon",
                "12",
                "Rare Holo GX",
                "3",
                "ENGLISH",
                "NEAR_MINT",
                "true",
                ""
        );

        when(cardRepository.existsByExternalIdAndLanguageAndCondition(
                "sm1-12",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        )).thenReturn(true);

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(record);

        assertFalse(result.valid());
        assertEquals(3, result.quantity());
        assertEquals(
                "Card already exists in the collection.",
                result.error()
        );
    }

    @Test
    void shouldReturnCsvFileLine()
            throws IOException {
        String csv = String.join(
                "\n",
                getHeader(),
                getRow(
                        "sm1-12",
                        "Decidueye-GX",
                        "sm1",
                        "Sun & Moon",
                        "12",
                        "Rare Holo GX",
                        "1",
                        "ENGLISH",
                        "NEAR_MINT",
                        "false",
                        ""
                ),
                getRow(
                        "sm1-13",
                        "Incineroar-GX",
                        "sm1",
                        "Sun & Moon",
                        "13",
                        "Rare Holo GX",
                        "1",
                        "ENGLISH",
                        "NEAR_MINT",
                        "false",
                        ""
                )
        );

        List<CSVRecord> records = parse(csv);

        when(cardRepository.existsByExternalIdAndLanguageAndCondition(
                "sm1-13",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        )).thenReturn(false);

        CardImportPreviewItemResponse result =
                cardImportValidator.validate(records.get(1));

        assertEquals(3L, result.line());
    }

    private CSVRecord createRecord(
            String externalId,
            String name,
            String collectionId,
            String collection,
            String cardNumber,
            String rarity,
            String quantity,
            String language,
            String condition,
            String favorite,
            String notes
    ) throws IOException {
        String csv = String.join(
                "\n",
                getHeader(),
                getRow(
                        externalId,
                        name,
                        collectionId,
                        collection,
                        cardNumber,
                        rarity,
                        quantity,
                        language,
                        condition,
                        favorite,
                        notes
                )
        );

        return parse(csv).get(0);
    }

    private List<CSVRecord> parse(String csv) throws IOException {
        try (
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build()
                        .parse(new StringReader(csv))
        ) {
            return parser.getRecords();
        }
    }

    private String getHeader() {
        return String.join(
                ",",
                CardCsvHeaders.EXTERNAL_ID,
                CardCsvHeaders.NAME,
                CardCsvHeaders.COLLECTION_ID,
                CardCsvHeaders.COLLECTION,
                CardCsvHeaders.CARD_NUMBER,
                CardCsvHeaders.RARITY,
                CardCsvHeaders.QUANTITY,
                CardCsvHeaders.LANGUAGE,
                CardCsvHeaders.CONDITION,
                CardCsvHeaders.FAVORITE,
                CardCsvHeaders.NOTES
        );
    }

    private String getRow(
            String externalId,
            String name,
            String collectionId,
            String collection,
            String cardNumber,
            String rarity,
            String quantity,
            String language,
            String condition,
            String favorite,
            String notes
    ) {
        return String.join(
                ",",
                externalId,
                name,
                collectionId,
                collection,
                cardNumber,
                rarity,
                quantity,
                language,
                condition,
                favorite,
                notes
        );
    }
}