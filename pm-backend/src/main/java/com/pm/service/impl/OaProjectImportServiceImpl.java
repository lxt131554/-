package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.dto.OaProjectImportResult;
import com.pm.dto.OaProjectImportRow;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.service.OaProjectImportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OaProjectImportServiceImpl implements OaProjectImportService {

    private final SysProjectMapper projectMapper;
    private final SysProjectMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OaProjectWorkbookParser parser = new OaProjectWorkbookParser();

    @Override
    @Transactional
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
            importOne(row, operatorUserId, result);
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

    private void importOne(OaProjectImportRow row, Long operatorUserId, OaProjectImportResult result) {
        OaProjectImportResult.Item item = new OaProjectImportResult.Item();
        item.setRowNumber(row.getRowNumber());
        item.setProjectName(row.getProjectName());
        item.setContractNo(row.getContractNo());
        item.setManagerName(row.getManagerName());

        try {
            SysProject project = findExistingProject(row);
            boolean creating = project == null;
            if (creating) {
                project = new SysProject();
                project.setCreateUserId(operatorUserId);
            }
            applyRow(project, row);

            if (creating) {
                projectMapper.insert(project);
                result.setCreatedCount(result.getCreatedCount() + 1);
                item.setAction("created");
            } else {
                projectMapper.updateById(project);
                result.setUpdatedCount(result.getUpdatedCount() + 1);
                item.setAction("updated");
            }

            item.setProjectId(project.getId());
            linkManager(project.getId(), row, result, item);
        } catch (Exception e) {
            result.setSkippedCount(result.getSkippedCount() + 1);
            item.setAction("skipped");
            item.setMessage(e.getMessage());
        }
        result.addItem(item);
    }

    private SysProject findExistingProject(OaProjectImportRow row) {
        if (hasText(row.getContractNo())) {
            SysProject byContract = projectMapper.selectOne(new LambdaQueryWrapper<SysProject>()
                    .like(SysProject::getProcurementInfo, row.getContractNo())
                    .last("LIMIT 1"));
            if (byContract != null) {
                return byContract;
            }
        }
        return projectMapper.selectOne(new LambdaQueryWrapper<SysProject>()
                .eq(SysProject::getName, row.getProjectName())
                .last("LIMIT 1"));
    }

    private void applyRow(SysProject project, OaProjectImportRow row) {
        project.setName(row.getProjectName());
        project.setStatus(resolveProjectStatus(row));
        project.setDescription(buildDescription(row));
        project.setContacts(joinLines(
                line("合同甲方", row.getCustomerName()),
                line("OA项目负责人", row.getManagerName())
        ));
        project.setAchievementType(joinText(row.getCategory(), row.getSubCategory(), " / "));
        project.setBidSituation(line("OA投标状态", row.getBidStatus()));
        project.setProcurementInfo(joinLines(
                line("合同编号", row.getContractNo()),
                line("合同是否签订", row.getContractSigned()),
                line("创建时间", row.getCreateDate()),
                line("签订时间", row.getSignDate())
        ));
        project.setAcquisitionResult(joinLines(
                line("合同产值", row.getContractOutputValue()),
                line("签订金额", row.getSignedAmount()),
                line("已开票总额", row.getInvoicedTotal()),
                line("已收总额", row.getReceivedTotal()),
                line("今年开票", row.getCurrentYearInvoice()),
                line("应收账款", row.getReceivable()),
                line("今年收款", row.getCurrentYearReceived()),
                line("已结算金额", row.getSettledAmount())
        ));
        project.setHrAllocation(line("项目负责人", row.getManagerName()));
    }

    private void linkManager(Long projectId, OaProjectImportRow row,
                             OaProjectImportResult result,
                             OaProjectImportResult.Item item) {
        if (!hasText(row.getManagerName())) {
            result.addMissingManager("");
            item.setMessage("OA项目负责人为空");
            return;
        }

        SysUser manager = resolveManager(row.getManagerName(), result, item);

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

    private boolean usernameExists(String username) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        return count != null && count > 0;
    }

    private String resolveProjectStatus(OaProjectImportRow row) {
        String bidStatus = row.getBidStatus();
        if (hasText(bidStatus) && (bidStatus.contains("完成") || bidStatus.contains("结项") || bidStatus.contains("归档"))) {
            return "completed";
        }
        return "active";
    }

    private String buildDescription(OaProjectImportRow row) {
        return joinLines(
                "OA导入项目",
                line("合同编号", row.getContractNo()),
                line("合同甲方", row.getCustomerName())
        );
    }

    private String line(String label, String value) {
        return hasText(value) ? label + "：" + value : "";
    }

    private String joinText(String left, String right, String delimiter) {
        if (hasText(left) && hasText(right)) {
            return left + delimiter + right;
        }
        if (hasText(left)) {
            return left;
        }
        return hasText(right) ? right : "";
    }

    private String joinLines(String... lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (!hasText(line)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(line);
        }
        return builder.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
