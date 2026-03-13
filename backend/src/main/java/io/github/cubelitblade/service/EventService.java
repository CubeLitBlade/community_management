package io.github.cubelitblade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService extends ServiceImpl<EventMapper, Event> {
    private final EventMapper eventMapper;

    /**
     * 领取事件表中处于 {@code waiting} 状态的事件。
     * <p>在数据表中查询服务器时间超出 {@code next_run_at} 字段，
     * 且状态为 {@code waiting} 的事件。并将其状态置为 {@code pending}。</p>
     * @param limit 领取事件的数量。
     * @return 所领取事件的事件表。
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Event> claimEvents(int limit) {
        LambdaQueryWrapper<Event> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Event::getStatus, Event.EventStatus.WAITING)
                .le(Event::getNextRunAt, LocalDateTime.now())
                .orderByAsc(Event::getNextRunAt)
                .last("limit "+ limit + " for update skip locked");
        List<Event> eventList = eventMapper.selectList(lambdaQueryWrapper);

        for (Event event : eventList) {
            event.setStatus(Event.EventStatus.PENDING);
        }

        this.updateBatchById(eventList);
        return eventList;
    }
}
