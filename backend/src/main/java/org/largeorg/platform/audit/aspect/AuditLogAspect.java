package org.largeorg.platform.audit.aspect;

import cn.dev33.satoken.stp.StpUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.audit.entity.AuditOperationLog;
import org.largeorg.platform.audit.service.AuditOperationLogService;
import org.largeorg.platform.audit.util.AuditParamSanitizer;
import org.largeorg.platform.common.util.IpUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class AuditLogAspect {

    private final AuditOperationLogService operationLogService;

    public AuditLogAspect(AuditOperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(org.largeorg.platform.audit.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        AuditOperationLog log = new AuditOperationLog();
        log.setCreatedAt(LocalDateTime.now());

        populateUserInfo(log);
        populateRequestInfo(log);
        populateMethodInfo(joinPoint, log);
        populateParams(joinPoint, log);

        try {
            Object result = joinPoint.proceed();
            log.setResult("success");
            log.setCostMs(System.currentTimeMillis() - start);
            operationLogService.save(log);
            return result;
        } catch (Throwable e) {
            log.setResult("fail");
            log.setErrorMsg(AuditParamSanitizer.truncate(e.getMessage(), 512));
            log.setCostMs(System.currentTimeMillis() - start);
            operationLogService.save(log);
            throw e;
        }
    }

    private void populateUserInfo(AuditOperationLog log) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            log.setUserId(userId);
            log.setUsername(StpUtil.getSession().getString("username"));
        } catch (Exception ignored) {
            log.setUserId(0L);
            log.setUsername("unknown");
        }
    }

    private void populateRequestInfo(AuditOperationLog log) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                log.setRequestPath(request.getRequestURI());
                log.setRequestMethod(request.getMethod());
                log.setIp(IpUtils.getClientIp(request));
                log.setUserAgent(AuditParamSanitizer.truncate(request.getHeader("User-Agent"), 512));
            }
        } catch (Exception ignored) {
        }
    }

    private void populateMethodInfo(ProceedingJoinPoint joinPoint, AuditOperationLog log) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog annotation = method.getAnnotation(AuditLog.class);
        if (annotation != null) {
            log.setModule(annotation.module());
            log.setAction(annotation.action());
        }
    }

    private void populateParams(ProceedingJoinPoint joinPoint, AuditOperationLog log) {
        try {
            Object[] args = joinPoint.getArgs();
            Object[] filtered = AuditParamSanitizer.filterArgs(args);
            String params = AuditParamSanitizer.serializeAndMask(filtered);
            log.setRequestParams(params);
        } catch (Exception ignored) {
        }
    }
}
