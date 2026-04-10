package io.github.cubelitblade.event.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cubelitblade.event.model.Event;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventMapper extends BaseMapper<Event> {}
