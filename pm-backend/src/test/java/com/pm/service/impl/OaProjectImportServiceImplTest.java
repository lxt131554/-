package com.pm.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

class OaProjectImportServiceImplTest {

    @Test
    void delegatesToRowImporter() throws Exception {
        OaProjectRowImporter rowImporter = mock(OaProjectRowImporter.class);
        doNothing().when(rowImporter).importRow(any(), any(), any());
        OaProjectImportServiceImpl service = new OaProjectImportServiceImpl(rowImporter);
        assertNotNull(service);
    }
}
