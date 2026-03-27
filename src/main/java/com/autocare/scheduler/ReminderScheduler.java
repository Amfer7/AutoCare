package com.autocare.scheduler;

import com.autocare.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SINGLETON PATTERN
 * Spring @Component guarantees a single instance throughout the application.
 * SRP: only triggers the evaluation job — all logic in ReminderService.
 * DIP: depends on ReminderService interface.
 *
 * Schedules:
 *   - Daily at midnight (cron)
 *   - Once on startup after 5 minutes (initialDelay)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderService reminderService;

    @Scheduled(cron = "0 0 0 * * *")
    public void evaluateReminders() {
        log.info("━━━ ReminderScheduler: daily evaluation run ━━━");
        try {
            reminderService.evaluateAllActive();
        } catch (Exception e) {
            log.error("━━━ ReminderScheduler failed: {}", e.getMessage(), e);
        }
    }

    @Scheduled(initialDelay = 300_000, fixedRate = Long.MAX_VALUE)
    public void evaluateOnStartup() {
        log.info("━━━ ReminderScheduler: startup evaluation run ━━━");
        reminderService.evaluateAllActive();
    }
}