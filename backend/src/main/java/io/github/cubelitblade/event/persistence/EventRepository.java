package io.github.cubelitblade.event.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Status;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventRepository {

  private final EventMapper eventMapper;

  @Transactional(readOnly = true)
  public List<Event> findWaitingEvents(int count, Instant now) {
    if (count <= 0) {
      throw new IllegalArgumentException("Count must be greater than 0");
    }

    LambdaQueryWrapper<EventPo> query =
        new QueryWrapper<EventPo>()
            .lambda()
            .eq(EventPo::getStatus, Status.WAITING.getValue())
            .le(EventPo::getNextRunAt, now)
            .orderByAsc(EventPo::getNextRunAt)
            .last("limit " + count);

    return eventMapper.selectList(query).stream()
        .peek(
            po ->
                log.info(
                    "[DEBUG][Event #{}] EventPo.payload is null? {}, class: {}",
                    po.getId(),
                    po.getPayload() == null,
                    po.getPayload() != null ? po.getPayload().getClass().getSimpleName() : "N/A"))
        .map(EventPo::toEvent)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<Event> findZombieEvents(int count, Instant threshold, Instant now) {
    if (count <= 0) {
      throw new IllegalArgumentException("Count must be greater than 0");
    }

    LambdaQueryWrapper<EventPo> query =
        new QueryWrapper<EventPo>()
            .lambda()
            .eq(EventPo::getStatus, Status.RUNNING.getValue())
            .le(EventPo::getUpdatedAt, threshold)
            .orderByAsc(EventPo::getUpdatedAt)
            .last("limit " + count);

    return eventMapper.selectList(query).stream()
        .map(EventPo::toEvent)
        .collect(Collectors.toList());
  }

  public boolean tryUpdate(Event event) {
    EventPo eventPo = EventPo.of(event);
    boolean succeedUpdate = eventMapper.updateById(eventPo) == 1;

    if (succeedUpdate) {
      event.tick();
    }

    return succeedUpdate;
  }

  public void updateOrThrow(Event event) {
    EventPo eventPo = EventPo.of(event);
    if (eventMapper.updateById(eventPo) != 1) {
      throw new IllegalStateException(
          "[Event #"
              + event.getId()
              + "] State update failed, possible concurrent modification or DB error");
    }
  }

  public void save(Event event) {
    eventMapper.insert(EventPo.of(event));
  }

  public Event find(long id) {
    return eventMapper.selectById(id).toEvent();
  }
}
