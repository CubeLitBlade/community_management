package io.github.cubelitblade.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.github.cubelitblade.configuration.TimeConfig;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepository {
    private final EventMapper eventMapper;
    private final TimeConfig timeConfig;

    @Transactional
    public List<Event> claimWaitingEvents(int count) {
        LambdaQueryWrapper<Event> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Event::getStatus, Event.EventStatus.WAITING)
                .le(Event::getNextRunAt, Instant.now())
                .orderByAsc(Event::getNextRunAt)
                .last("for update skip locked limit " + count);
        List<Event> eventList = eventMapper.selectList(lambdaQueryWrapper);

        for (Event event : eventList) {
            event.run(timeConfig.now());
        }

        updateOrThrow(eventList);
        return eventList;
    }

    @Transactional
    public void resetZombieEvents(Instant threshold) {
        LambdaQueryWrapper<Event> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Event::getStatus, Event.EventStatus.RUNNING)
                .le(Event::getUpdatedAt, threshold)
                .orderByAsc(Event::getUpdatedAt)
                .last("for update skip locked");
        List<Event> eventList = eventMapper.selectList(lambdaQueryWrapper);

        for (Event event : eventList) {
            event.await(timeConfig.now(), timeConfig.now());
        }

        updateOrThrow(eventList);
    }

    public void save(Event event) {
        eventMapper.insert(event);
    }

    public void saveOrThrow(Event event) {
        if (eventMapper.insert(event) != 1) {
            throw new RuntimeException("Failed to save event: " + event);
        }
    }

    public void update(Event event) {
        eventMapper.updateById(event);
    }

    @Transactional
    public void update(List<Event> events) {
        eventMapper.updateById(events);
    }

    public void updateOrThrow(Event event) {
        if (eventMapper.updateById(event) != 1) {
            throw new RuntimeException("Failed to update event: " + event);
        }
    }

    @Transactional
    public void updateOrThrow(List<Event> events) {
        List<BatchResult> results = eventMapper.updateById(events);
        for (BatchResult result : results) {
            if (result.getUpdateCounts()[0] != 1) {
                throw new RuntimeException("Failed to update event: " + result);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEventStep(Event event) {
        LambdaUpdateWrapper<Event> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Event::getId, event.getId()).set(Event::getCurrentStep, event.getCurrentStep());
        if (eventMapper.update(lambdaUpdateWrapper) != 1) {
            throw new RuntimeException("Failed to update event: " + event);
        }
    }

    public Event findById(long id) {
        return eventMapper.selectById(id);
    }
}
