package com.cardex.api.component.cardimport;

import com.cardex.api.exception.InvalidCsvFileException;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardCsvReaderTest {

    private CardCsvReader cardCsvReader;

    @BeforeEach
    void setUp() {
        cardCsvReader = new CardCsvReader();
    }

    @Test
    void shouldReadValidCsvFile() {
        String content = """
                External ID,Name,Collection ID,Collection,Card Number,Rarity,Quantity,Language,Condition,Favorite,Notes
                xy1-122,Professor Sycamore,xy1,XY,122,Uncommon,1,ENGLISH,NEAR_MINT,false,Test
                """;

        MockMultipartFile file = createCsvFile(content);

        List<CSVRecord> records = cardCsvReader.read(file);

        assertEquals(1, records.size());
        assertEquals("xy1-122", records.get(0).get(CardCsvHeaders.EXTERNAL_ID));
        assertEquals("Professor Sycamore", records.get(0).get(CardCsvHeaders.NAME));
    }

    @Test
    void shouldReadCsvFileWithUtf8Bom() {
        String content = "\uFEFF"
                + """
                External ID,Name,Collection ID,Collection,Card Number,Rarity,Quantity,Language,Condition,Favorite,Notes
                xy1-122,Professor Sycamore,xy1,XY,122,Uncommon,1,ENGLISH,NEAR_MINT,false,Test
                """;

        MockMultipartFile file = createCsvFile(content);

        List<CSVRecord> records = cardCsvReader.read(file);

        assertEquals(1, records.size());
        assertEquals("xy1-122", records.get(0).get(CardCsvHeaders.EXTERNAL_ID));
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        InvalidCsvFileException exception = assertThrows(
                InvalidCsvFileException.class,
                () -> cardCsvReader.read(file)
        );

        assertEquals(
                "The CSV file is required.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectFileWithoutCsvExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "collection.txt",
                "text/plain",
                "test".getBytes(StandardCharsets.UTF_8)
        );

        InvalidCsvFileException exception = assertThrows(
                InvalidCsvFileException.class,
                () -> cardCsvReader.read(file)
        );

        assertEquals(
                "The file must have the .csv extension.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectCsvWithoutExpectedHeaders() {
        String content = """
                Name,Quantity
                Professor Sycamore,1
                """;

        MockMultipartFile file = createCsvFile(content);

        InvalidCsvFileException exception = assertThrows(
                InvalidCsvFileException.class,
                () -> cardCsvReader.read(file)
        );

        assertEquals(
                "The CSV file does not contain the expected CardDex columns.",
                exception.getMessage()
        );
    }

    private MockMultipartFile createCsvFile(String content) {
        return new MockMultipartFile(
                "file",
                "cardex-collection.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}