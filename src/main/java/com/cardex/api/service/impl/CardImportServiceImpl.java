package com.cardex.api.service.impl;

import com.cardex.api.component.cardimport.CardCsvReader;
import com.cardex.api.component.cardimport.CardImportValidator;
import com.cardex.api.dto.response.CardImportPreviewItemResponse;
import com.cardex.api.dto.response.CardImportPreviewResponse;
import com.cardex.api.service.CardImportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardImportServiceImpl implements CardImportService {

    private final CardCsvReader cardCsvReader;
    private final CardImportValidator cardImportValidator;

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
}