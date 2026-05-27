package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.security.LoginUser;
import com.pm.service.SysDeviationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deviations")
@RequiredArgsConstructor
public class DeviationController {

    private final SysDeviationService deviationService;

    @GetMapping
    public Result<List<SysDeviation>> list(
            @RequestParam(required = false) Long projectId) {
        List<SysDeviation> list = deviationService.listByProject(projectId);
        return Result.ok(list);
    }

    @PostMapping
    public Result<SysDeviation> create(@RequestBody SysDeviation deviation,
                                       @AuthenticationPrincipal LoginUser loginUser) {
        deviation.setType("manual");
        deviation.setStatus("open");
        deviation.setCreateUserId(loginUser.getUser().getId());
        deviationService.save(deviation);
        return Result.ok(deviation);
    }

    @PutMapping("/{id}/close")
    public Result<?> close(@PathVariable Long id) {
        deviationService.closeDeviation(id);
        return Result.ok();
    }
}
