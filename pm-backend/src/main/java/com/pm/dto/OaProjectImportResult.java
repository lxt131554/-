package com.pm.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OaProjectImportResult {
    private int totalRows;
    private int createdCount;
    private int updatedCount;
    private int skippedCount;
    private int matchedManagerCount;
    private int createdManagerUserCount;
    private int missingManagerCount;
    private List<String> missingManagers = new ArrayList<>();
    private List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }

    public void addMissingManager(String managerName) {
        if (managerName != null && !managerName.isBlank() && !missingManagers.contains(managerName)) {
            missingManagers.add(managerName);
        }
        missingManagerCount++;
    }

    @Data
    public static class Item {
        private int rowNumber;
        private Long projectId;
        private String projectName;
        private String contractNo;
        private String managerName;
        private String action;
        private String message;
    }
}
