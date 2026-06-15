package com.pm.dto;

import lombok.Data;

@Data
public class OaProjectImportRow {
    private int rowNumber;
    private String bidStatus;
    private String contractNo;
    private String contractSigned;
    private String createDate;
    private String projectName;
    private String managerName;
    private String category;
    private String subCategory;
    private String customerName;
    private String signDate;
    private String contractOutputValue;
    private String signedAmount;
    private String invoicedTotal;
    private String receivedTotal;
    private String currentYearInvoice;
    private String receivable;
    private String currentYearReceived;
    private String settledAmount;
}
