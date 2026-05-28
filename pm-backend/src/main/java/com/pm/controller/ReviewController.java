package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysReview;
import com.pm.security.LoginUser;
import com.pm.service.SysReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/review")
@RequiredArgsConstructor
public class ReviewController {

    private final SysReviewService reviewService;

    @GetMapping
    public Result<SysReview> get(@PathVariable Long projectId) {
        SysReview review = reviewService.getByProjectId(projectId);
        return Result.ok(review);
    }

    @PostMapping
    public Result<SysReview> save(@PathVariable Long projectId,
                                  @RequestBody SysReview review,
                                  @AuthenticationPrincipal LoginUser loginUser) {
        review.setProjectId(projectId);
        SysReview saved = reviewService.saveOrUpdateReview(review, loginUser.getUser().getId());
        return Result.ok(saved);
    }
}
