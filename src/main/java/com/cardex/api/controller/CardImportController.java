package com.cardex.api.controller;

import com.cardex.api.dto.response.CardImportPreviewResponse;
import com.cardex.api.dto.response.CardImportResponse;
import com.cardex.api.service.CardImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cards/import")
@RequiredArgsConstructor
public class CardImportController {

    private final CardImportService cardImportService;

    @PostMapping(
            value = "/csv/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CardImportPreviewResponse> previewCsv(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                cardImportService.previewCsv(file)
        );
    }

    @PostMapping(
            value = "/csv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CardImportResponse> importCsv(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                cardImportService.importCsv(file)
        );
    }
}