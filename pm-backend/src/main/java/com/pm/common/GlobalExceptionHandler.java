package com.pm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDenied(AccessDeniedException e) {
        return Result.fail(403, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleBadRequest(IllegalArgumentException e) {
        log.warn("Bad request", e);
        return Result.fail(400, e.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public Result<?> handleMultipart(MultipartException e) {
        log.warn("Multipart upload error", e);
        return Result.fail(400, "文件上传失败，请确认选择的是 OA 导出的 .xls/.xlsx 文件后重试");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("Missing request parameter", e);
        return Result.fail(400, "缺少上传文件，请重新选择 OA Excel 文件");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntime(RuntimeException e) {
        log.error("Runtime error", e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("Unknown error", e);
        return Result.fail("服务器内部错误");
    }
}
