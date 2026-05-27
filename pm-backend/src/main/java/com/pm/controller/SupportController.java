package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysSupportItem;
import com.pm.security.LoginUser;
import com.pm.service.SysSupportItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supports")
@RequiredArgsConstructor
public class SupportController {

    private final SysSupportItemService supportItemService;

    @GetMapping
    public Result<List<SysSupportItem>> list(
            @RequestParam(required = false) String status) {
        List<SysSupportItem> list = supportItemService.listAll(status);
        return Result.ok(list);
    }

    @PostMapping
    public Result<SysSupportItem> create(@RequestBody SysSupportItem item,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        item.setApplicantId(loginUser.getUser().getId());
        item.setStatus("pending");
        supportItemService.save(item);
        return Result.ok(item);
    }

    @PutMapping("/{id}/resolve")
    public Result<?> resolve(@PathVariable Long id,
                             @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        supportItemService.resolve(id, reply);
        return Result.ok();
    }
}
