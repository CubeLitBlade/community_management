package io.github.cubelitblade.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Worker {
    @Scheduled(fixedDelay = 5000)
    public void run() {
        log.info("Worker running...");
    }
}
