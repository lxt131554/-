package com.pm.controller;

import com.pm.entity.SysProject;
import com.pm.security.LoginUser;
import com.pm.service.ProjectExportService;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectExportController {

    private final ProjectExportService exportService;
    private final SysProjectService projectService;
    private final ProjectAccessService accessService;

    @GetMapping("/projects/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id,
                                         @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(id, loginUser.getUser());
        SysProject project = projectService.getById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = exportService.exportProject(id);
        String filename = URLEncoder.encode(project.getName() + "_台账.xlsx", StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
