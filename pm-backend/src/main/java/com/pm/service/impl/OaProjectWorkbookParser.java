package com.pm.service.impl;

import com.pm.dto.OaProjectImportRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class OaProjectWorkbookParser {

    private static final String PROJECT_HEADER = "项目";
    private static final String MANAGER_HEADER = "项目负责人";
    private final DataFormatter formatter = new DataFormatter(Locale.CHINA);

    List<OaProjectImportRow> parse(Workbook workbook) {
        if (workbook == null || workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException("workbook is empty");
        }

        Sheet sheet = workbook.getSheetAt(0);
        int headerRowIndex = findHeaderRow(sheet);
        Row headerRow = sheet.getRow(headerRowIndex);
        Map<String, Integer> headerMap = buildHeaderMap(headerRow);
        requireHeader(headerMap, PROJECT_HEADER);
        requireHeader(headerMap, MANAGER_HEADER);

        List<OaProjectImportRow> rows = new ArrayList<>();
        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String projectName = text(row, headerMap, PROJECT_HEADER);
            if (!hasText(projectName)) {
                continue;
            }
            rows.add(toImportRow(row, headerMap, i + 1, projectName));
        }
        return rows;
    }

    private int findHeaderRow(Sheet sheet) {
        int max = Math.min(sheet.getLastRowNum(), 10);
        for (int i = 0; i <= max; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Map<String, Integer> headerMap = buildHeaderMap(row);
            if (headerMap.containsKey(PROJECT_HEADER) && headerMap.containsKey(MANAGER_HEADER)) {
                return i;
            }
        }
        throw new IllegalArgumentException("OA Excel header row not found");
    }

    private Map<String, Integer> buildHeaderMap(Row row) {
        Map<String, Integer> map = new HashMap<>();
        if (row == null) {
            return map;
        }
        for (Cell cell : row) {
            String value = formatter.formatCellValue(cell).trim();
            if (hasText(value)) {
                map.put(value, cell.getColumnIndex());
            }
        }
        return map;
    }

    private OaProjectImportRow toImportRow(Row row, Map<String, Integer> headerMap, int rowNumber, String projectName) {
        OaProjectImportRow item = new OaProjectImportRow();
        item.setRowNumber(rowNumber);
        item.setBidStatus(text(row, headerMap, "投标状态"));
        item.setContractNo(text(row, headerMap, "合同编号"));
        item.setContractSigned(text(row, headerMap, "合同是否签订"));
        item.setCreateDate(text(row, headerMap, "创建时间"));
        item.setProjectName(projectName);
        item.setManagerName(text(row, headerMap, MANAGER_HEADER));
        item.setCategory(text(row, headerMap, "所属类型"));
        item.setSubCategory(text(row, headerMap, "所属子类"));
        item.setCustomerName(text(row, headerMap, "合同甲方"));
        item.setSignDate(text(row, headerMap, "签订时间"));
        item.setContractOutputValue(text(row, headerMap, "合同产值"));
        item.setSignedAmount(text(row, headerMap, "签订金额"));
        item.setInvoicedTotal(text(row, headerMap, "已开票总额"));
        item.setReceivedTotal(text(row, headerMap, "已收总额"));
        item.setCurrentYearInvoice(text(row, headerMap, "今年开票"));
        item.setReceivable(text(row, headerMap, "应收账款"));
        item.setCurrentYearReceived(text(row, headerMap, "今年收款"));
        item.setSettledAmount(text(row, headerMap, "已结算金额"));
        return item;
    }

    private String text(Row row, Map<String, Integer> headerMap, String header) {
        Integer columnIndex = headerMap.get(header);
        if (columnIndex == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(columnIndex)).trim();
    }

    private void requireHeader(Map<String, Integer> headerMap, String header) {
        if (!headerMap.containsKey(header)) {
            throw new IllegalArgumentException("OA Excel missing required header: " + header);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
