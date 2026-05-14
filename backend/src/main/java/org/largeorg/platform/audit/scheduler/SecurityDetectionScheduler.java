package org.largeorg.platform.audit.scheduler;

import org.largeorg.platform.audit.service.SecurityDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SecurityDetectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SecurityDetectionScheduler.class);

    private final SecurityDetectionService detectionService;

    public SecurityDetectionScheduler(SecurityDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @Scheduled(fixedDelay = 300_000)
    public void executeDetection() {
        log.debug("安全审计定时检测开始");
        try {
            detectionService.runAllChecks();
        } catch (Exception e) {
            log.error("安全审计定时检测异常", e);
        }
        log.debug("安全审计定时检测结束");
    }
}
