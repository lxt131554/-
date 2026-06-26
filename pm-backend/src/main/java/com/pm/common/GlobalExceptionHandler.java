package com.pm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("；"));
        return Result.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("；"));
        return Result.fail(400, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Unreadable request body", e);
        return Result.fail(400, "请求参数格式错误，请检查填写内容");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return Result.fail(400, "数据约束冲突，请检查关联项目、阶段或人员是否存在");
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
