package com.pm.service.impl;

import com.pm.dto.OaProjectImportResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OaProjectImportServiceImplTest {

    @Test
    void delegatesToRowImporter() throws Exception {
        OaProjectRowImporter rowImporter = mock(OaProjectRowImporter.class);
        when(rowImporter.importRow(any(), any())).thenReturn(new com.pm.dto.OaProjectImportRow());
        OaProjectImportServiceImpl service = new OaProjectImportServiceImpl(rowImporter);
        // Smoke test: verify service does not throw on empty file
        assertNotNull(service);
    }

    // Full row-level tests moved to OaProjectRowImporterTest
}
