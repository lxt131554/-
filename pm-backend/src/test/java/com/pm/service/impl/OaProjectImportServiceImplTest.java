package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.dto.OaProjectImportResult;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysUserMapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OaProjectImportServiceImplTest {

    @Test
    void createsMissingManagerUserAndBindsProjectMember() throws Exception {
        SysProjectMapper projectMapper = mock(SysProjectMapper.class);
        SysProjectMemberMapper memberMapper = mock(SysProjectMemberMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        OaProjectImportServiceImpl service = new OaProjectImportServiceImpl(
                projectMapper, memberMapper, userMapper, passwordEncoder);

        SysProject existingProject = new SysProject();
        existingProject.setId(99L);
        existingProject.setName("测试项目");
        when(projectMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingProject);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-123456");
        when(userMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(7L);
            return 1;
        });
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        OaProjectImportResult result = service.importProjects(buildWorkbookFile(), 1L);

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insert(userCaptor.capture());
        SysUser createdUser = userCaptor.getValue();
        assertEquals("彭成", createdUser.getRealName());
        assertEquals("pengcheng", createdUser.getUsername());
        assertEquals("encoded-123456", createdUser.getPassword());
        assertEquals("manager", createdUser.getRole());
        assertEquals(Boolean.TRUE, createdUser.getEnabled());

        ArgumentCaptor<SysProjectMember> memberCaptor = ArgumentCaptor.forClass(SysProjectMember.class);
        verify(memberMapper).insert(memberCaptor.capture());
        SysProjectMember member = memberCaptor.getValue();
        assertEquals(99L, member.getProjectId());
        assertEquals(7L, member.getUserId());
        assertEquals("manager", member.getRoleInProject());
        assertEquals("confirmed", member.getStatus());

        assertEquals(1, result.getMatchedManagerCount());
        assertEquals(1, result.getCreatedManagerUserCount());
        assertEquals(0, result.getMissingManagerCount());
        assertFalse(result.getItems().isEmpty());
        assertEquals("负责人账号已自动创建并匹配", result.getItems().get(0).getMessage());
    }

    private MockMultipartFile buildWorkbookFile() throws Exception {
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
        data.createCell(2).setCellValue("G202406029");
        data.createCell(5).setCellValue("测试项目");
        data.createCell(6).setCellValue("彭成");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        workbook.write(output);
        workbook.close();
        return new MockMultipartFile("file", "oa.xls", "application/vnd.ms-excel", output.toByteArray());
    }
}
