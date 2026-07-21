package com.cardex.api.component.cardimport;

import com.cardex.api.exception.InvalidCsvFileException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CardCsvReader {

    public List<CSVRecord> read(MultipartFile file) {
        validateFile(file);

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                BOMInputStream.builder()
                                        .setInputStream(file.getInputStream())
                                        .get(),
                                StandardCharsets.UTF_8
                        )
                );

                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {
            validateHeaders(parser);

            return new ArrayList<>(parser.getRecords());
        } catch (InvalidCsvFileException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidCsvFileException(
                    "Could not read the CSV file.",
                    exception
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCsvFileException(
                    "The CSV file is required."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null
                || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new InvalidCsvFileException(
                    "The file must have the .csv extension."
            );
        }
    }

    private void validateHeaders(CSVParser parser) {
        if (!parser.getHeaderMap()
                .keySet()
                .containsAll(CardCsvHeaders.requiredHeaders())) {
            throw new InvalidCsvFileException(
                    "The CSV file does not contain the expected CardDex columns."
            );
        }
    }
}