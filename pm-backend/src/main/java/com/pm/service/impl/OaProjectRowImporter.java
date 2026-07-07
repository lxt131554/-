package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.dto.OaProjectImportResult;
import com.pm.dto.OaProjectImportRow;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectContract;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectContractMapper;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
class OaProjectRowImporter {

    private final SysProjectMapper projectMapper;
    private final SysProjectContractMapper contractMapper;
    private final SysProjectMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void importRow(OaProjectImportRow row, Long operatorUserId, OaProjectImportResult result) {
        OaProjectImportResult.Item item = new OaProjectImportResult.Item();
        item.setRowNumber(row.getRowNumber());
        item.setProjectName(row.getProjectName());
        item.setContractNo(row.getContractNo());
        item.setManagerName(row.getManagerName());

        try {
            // Step 1: Validate contract number
            String contractNo = row.getContractNo();
            if (!hasText(contractNo)) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                item.setAction("skipped");
                item.setMessage("合同编号为空，跳过该行");
                result.addItem(item);
                return;
            }

            // Step 2: Find existing contract by contractNo (exact match)
            SysProjectContract existingContract = contractMapper.selectOne(
                    new LambdaQueryWrapper<SysProjectContract>()
                            .eq(SysProjectContract::getContractNo, contractNo)
                            .last("LIMIT 1"));

            // Step 3: Resolve manager user
            SysUser manager = null;
            if (hasText(row.getManagerName())) {
                manager = resolveManager(row.getManagerName(), result, item);
            }

            if (existingContract != null) {
                // Contract exists — update contract + update project name only + link manager
                updateExisting(existingContract, row, manager, operatorUserId, result, item);
            } else {
                // Contract not exists — create new project + new contract + link manager
                createNew(row, manager, operatorUserId, result, item);
            }
        } catch (Exception e) {
            result.setSkippedCount(result.getSkippedCount() + 1);
            item.setAction("skipped");
            item.setMessage("导入失败：" + e.getMessage());
        }
        result.addItem(item);
    }

    /**
     * Update scenario: contract already exists.
     * Only update project.name and contract OA fields. Never overwrite manual project data.
     */
    private void updateExisting(SysProjectContract contract, OaProjectImportRow row,
                                 SysUser manager, Long operatorUserId,
                                 OaProjectImportResult result, OaProjectImportResult.Item item) {
        // Update project name only — never touch planning/description/contacts/status
        SysProject project = projectMapper.selectById(contract.getProjectId());
        if (project == null) {
            // Contract points to a deleted project — treat as create
            createNew(row, manager, operatorUserId, result, item);
            return;
        }
        if (hasText(row.getProjectName()) && !row.getProjectName().equals(project.getName())) {
            project.setName(row.getProjectName());
            projectMapper.updateById(project);
        }

        // Update contract with latest OA data
        applyContractFields(contract, row);
        contractMapper.updateById(contract);

        // Link manager
        if (manager != null) {
            linkManager(project.getId(), manager, result, item);
        }

        result.setUpdatedCount(result.getUpdatedCount() + 1);
        item.setProjectId(project.getId());
        item.setAction("updated");
        if (!hasText(item.getMessage())) {
            item.setMessage("已更新合同及项目名称");
        }
    }

    /**
     * Create scenario: no existing contract.
     * Create new project with only basic fields, then create contract record.
     */
    private void createNew(OaProjectImportRow row, SysUser manager, Long operatorUserId,
                           OaProjectImportResult result, OaProjectImportResult.Item item) {
        // Create new project with basic fields only
        SysProject project = new SysProject();
        project.setName(row.getProjectName());
        project.setDescription(buildOaDescription(row));
        project.setStatus(resolveProjectStatus(row));
        project.setCreateUserId(operatorUserId);
        projectMapper.insert(project);

        // Create contract record with all OA fields
        SysProjectContract contract = new SysProjectContract();
        contract.setProjectId(project.getId());
        applyContractFields(contract, row);
        contractMapper.insert(contract);

        // Link manager
        if (manager != null) {
            linkManager(project.getId(), manager, result, item);
        }

        result.setCreatedCount(result.getCreatedCount() + 1);
        item.setProjectId(project.getId());
        item.setAction("created");
        if (!hasText(item.getMessage())) {
            item.setMessage("已创建项目及合同");
        }
    }

    /**
     * Apply OA row data to contract entity fields.
     */
    private void applyContractFields(SysProjectContract contract, OaProjectImportRow row) {
        contract.setContractNo(row.getContractNo());
        contract.setBidStatus(row.getBidStatus());
        contract.setContractSigned(row.getContractSigned());
        contract.setCustomerName(row.getCustomerName());
        contract.setSignDate(parseDate(row.getSignDate()));
        contract.setCategory(row.getCategory());
        contract.setSubCategory(row.getSubCategory());
        contract.setContractOutputValue(parseDecimal(row.getContractOutputValue()));
        contract.setSignedAmount(parseDecimal(row.getSignedAmount()));
        contract.setInvoicedTotal(parseDecimal(row.getInvoicedTotal()));
        contract.setReceivedTotal(parseDecimal(row.getReceivedTotal()));
        contract.setReceivable(parseDecimal(row.getReceivable()));
        contract.setSettledAmount(parseDecimal(row.getSettledAmount()));
        contract.setOaSyncTime(LocalDateTime.now());
    }

    /**
     * Build minimal description for OA-imported projects.
     */
    private String buildOaDescription(OaProjectImportRow row) {
        StringBuilder sb = new StringBuilder("OA导入项目");
        if (hasText(row.getContractNo())) {
            sb.append('\n').append("合同编号：").append(row.getContractNo());
        }
        if (hasText(row.getCustomerName())) {
            sb.append('\n').append("合同甲方：").append(row.getCustomerName());
        }
        return sb.toString();
    }

    /**
     * Ensure the project manager is a confirmed member of the project.
     */
    private void linkManager(Long projectId, SysUser manager,
                              OaProjectImportResult result, OaProjectImportResult.Item item) {
        SysProjectMember member = memberMapper.selectOne(new LambdaQueryWrapper<SysProjectMember>()
                .eq(SysProjectMember::getProjectId, projectId)
                .eq(SysProjectMember::getUserId, manager.getId())
                .eq(SysProjectMember::getRoleInProject, "manager")
                .last("LIMIT 1"));
        if (member == null) {
            member = new SysProjectMember();
            member.setProjectId(projectId);
            member.setUserId(manager.getId());
            member.setRoleInProject("manager");
            member.setStatus("confirmed");
            memberMapper.insert(member);
        } else if (!"confirmed".equals(member.getStatus())) {
            member.setStatus("confirmed");
            memberMapper.updateById(member);
        }

        result.setMatchedManagerCount(result.getMatchedManagerCount() + 1);
        if (!hasText(item.getMessage())) {
            item.setMessage("负责人已匹配");
        }
    }

    /**
     * Resolve manager by real name. Auto-create user account if not found.
     */
    private SysUser resolveManager(String managerName, OaProjectImportResult result, OaProjectImportResult.Item item) {
        SysUser manager = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRealName, managerName)
                .last("LIMIT 1"));
        if (manager != null) {
            return manager;
        }

        manager = new SysUser();
        manager.setRealName(managerName);
        manager.setUsername(PinyinUsernameGenerator.generate(managerName, this::usernameExists));
        manager.setPassword(passwordEncoder.encode("123456"));
        manager.setRole("manager");
        manager.setDept("");
        manager.setEnabled(true);
        userMapper.insert(manager);
        result.setCreatedManagerUserCount(result.getCreatedManagerUserCount() + 1);
        item.setMessage("负责人账号已自动创建并匹配");
        return manager;
    }

    /**
     * Resolve project status from OA bid status.
     */
    private String resolveProjectStatus(OaProjectImportRow row) {
        String bidStatus = row.getBidStatus();
        if (hasText(bidStatus) && (bidStatus.contains("完成") || bidStatus.contains("结项") || bidStatus.contains("归档"))) {
            return "completed";
        }
        return "active";
    }

    private boolean usernameExists(String username) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        return count != null && count > 0;
    }

    private LocalDate parseDate(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            String cleaned = value.trim();
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
