package com.cardex.api.controller;

import com.cardex.api.dto.response.ShareStatusResponse;
import com.cardex.api.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @GetMapping
    public ResponseEntity<ShareStatusResponse> getStatus() {
        return ResponseEntity.ok(shareService.getStatus());
    }

    @PostMapping("/enable")
    public ResponseEntity<ShareStatusResponse> enable() {
        return ResponseEntity.ok(shareService.enable());
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disable() {
        shareService.disable();
        return ResponseEntity.noContent().build();
    }
}
