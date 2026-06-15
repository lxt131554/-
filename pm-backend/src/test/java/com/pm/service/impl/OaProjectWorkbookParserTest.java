package com.pm.service.impl;

import com.pm.dto.OaProjectImportRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OaProjectWorkbookParserTest {

    private final OaProjectWorkbookParser parser = new OaProjectWorkbookParser();

    @Test
    void parsesFixedOaContractExportHeaders() {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Worksheet");
        Row header = sheet.createRow(0);
        String[] headers = {
                "序号", "投标状态", "合同编号", "合同是否签订", "创建时间", "项目", "项目负责人",
                "所属类型", "所属子类", "合同甲方", "签订时间", "合同产值", "签订金额", "已开票总额",
                "已收总额", "今年开票", "应收账款", "今年收款", "已结算金额"
        };
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
        Row data = sheet.createRow(1);
        data.createCell(1).setCellValue("项目立项");
        data.createCell(2).setCellValue("G202406029");
        data.createCell(3).setCellValue("已签");
        data.createCell(4).setCellValue("2024-12-11");
        data.createCell(5).setCellValue("遂宁市碳汇监测科普及古树名木监测试点技术服务项目");
        data.createCell(6).setCellValue("高飞");
        data.createCell(7).setCellValue("资源调查和监测");
        data.createCell(8).setCellValue("生态综合监测");
        data.createCell(9).setCellValue("遂宁市林业局");
        data.createCell(10).setCellValue("2024-12-11");
        data.createCell(11).setCellValue(168000);
        data.createCell(12).setCellValue(168000);

        List<OaProjectImportRow> rows = parser.parse(workbook);

        assertEquals(1, rows.size());
        assertEquals("G202406029", rows.get(0).getContractNo());
        assertEquals("遂宁市碳汇监测科普及古树名木监测试点技术服务项目", rows.get(0).getProjectName());
        assertEquals("高飞", rows.get(0).getManagerName());
        assertEquals("168000", rows.get(0).getSignedAmount());
    }

    @Test
    void rejectsWorkbookWithoutRequiredProjectHeader() {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("bad");
        sheet.createRow(0).createCell(0).setCellValue("合同编号");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(workbook));
    }
}
