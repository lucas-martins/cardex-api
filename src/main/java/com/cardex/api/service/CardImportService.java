package com.cardex.api.service;

import com.cardex.api.dto.response.CardImportPreviewResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CardImportService {

    CardImportPreviewResponse previewCsv(MultipartFile file);
}