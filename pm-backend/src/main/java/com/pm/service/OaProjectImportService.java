package com.pm.service;

import com.pm.dto.OaProjectImportResult;
import org.springframework.web.multipart.MultipartFile;

public interface OaProjectImportService {
    OaProjectImportResult importProjects(MultipartFile file, Long operatorUserId);
}
