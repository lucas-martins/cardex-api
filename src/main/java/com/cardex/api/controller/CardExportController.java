package com.cardex.api.controller;

import com.cardex.api.service.CardExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/cards/export")
@RequiredArgsConstructor
public class CardExportController {

    private final CardExportService cardExportService;

    @GetMapping(
            value = "/csv",
            produces = "text/csv"
    )
    public ResponseEntity<byte[]> exportCsv() {
        byte[] file = cardExportService.exportCsv();

        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(
                                "cardex-collection.csv",
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(file.length)
                .body(file);
    }
}