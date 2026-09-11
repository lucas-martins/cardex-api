package com.cardex.api.controller;

import com.cardex.api.dto.response.CollectionChecklistResponse;
import com.cardex.api.dto.response.PublicShareSummaryResponse;
import com.cardex.api.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/share")
@RequiredArgsConstructor
public class PublicShareController {

    private final ShareService shareService;

    @GetMapping("/{token}")
    public ResponseEntity<PublicShareSummaryResponse> getSummary(
            @PathVariable String token
    ) {
        return ResponseEntity.ok(shareService.getPublicSummary(token));
    }

    @GetMapping("/{token}/collections/{collectionId}/checklist")
    public ResponseEntity<CollectionChecklistResponse> getChecklist(
            @PathVariable String token,
            @PathVariable String collectionId
    ) {
        return ResponseEntity.ok(
                shareService.getPublicChecklist(token, collectionId)
        );
    }
}
