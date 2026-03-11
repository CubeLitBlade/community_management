package io.github.cubelitblade.worker;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.mapper.EventMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Worker {
    private final EventMapper eventMapper;

    @Scheduled(fixedDelay = 5000)
    public void run() {
        log.info("Worker running...");
        LambdaQueryWrapper<Event> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Event::getStatus, "pending")
                .le(Event::getNextRunAt, LocalDateTime.now())
                .orderByAsc(Event::getNextRunAt)
                .last("limit 10");
        List<Event> eventList = eventMapper.selectList(lambdaQueryWrapper);

        log.info("Event list size: {}", eventList.size());
    }
}
