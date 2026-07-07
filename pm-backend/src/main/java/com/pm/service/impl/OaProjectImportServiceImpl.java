package com.pm.service.impl;

import com.pm.dto.OaProjectImportResult;
import com.pm.dto.OaProjectImportRow;
import com.pm.service.OaProjectImportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OaProjectImportServiceImpl implements OaProjectImportService {

    private final OaProjectRowImporter rowImporter;
    private final OaProjectWorkbookParser parser = new OaProjectWorkbookParser();

    @Override
    public OaProjectImportResult importProjects(MultipartFile file, Long operatorUserId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要导入的 OA Excel 文件");
        }

        List<OaProjectImportRow> rows;
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = readWorkbook(file.getOriginalFilename(), inputStream)) {
            rows = parser.parse(workbook);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("OA文件读取失败，请确认上传的是OA导出的.xls/.xlsx文件：" + e.getMessage(), e);
        }

        OaProjectImportResult result = new OaProjectImportResult();
        result.setTotalRows(rows.size());
        for (OaProjectImportRow row : rows) {
            rowImporter.importRow(row, operatorUserId, result);
        }
        return result;
    }

    private Workbook readWorkbook(String filename, InputStream inputStream) throws IOException {
        String lowerName = filename == null ? "" : filename.toLowerCase();
        if (lowerName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        }
        if (lowerName.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        }
        return WorkbookFactory.create(inputStream);
    }
}
