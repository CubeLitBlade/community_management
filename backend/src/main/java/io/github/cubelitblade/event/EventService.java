package io.github.cubelitblade.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService extends ServiceImpl<EventMapper, Event> {
    private final EventMapper eventMapper;
    private final ObjectMapper objectMapper;

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
                .last("for update skip locked limit "+ limit);
        List<Event> eventList = eventMapper.selectList(lambdaQueryWrapper);

        for (Event event : eventList) {
            event.setStatus(Event.EventStatus.RUNNING);
        }

        this.updateBatchById(eventList);
        return eventList;
    }

    @Transactional
    public void createDemoEvent(DemoEventPayload payload) {
        Event event = Event.builder()
                .type(Event.EventType.DEMO_EVENT)
                .status(Event.EventStatus.WAITING)
                .payload(objectMapper.valueToTree(payload))
                .build();

        this.save(event);
    }
}
