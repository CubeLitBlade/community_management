package io.github.cubelitblade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cubelitblade.entity.Event;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventMapper extends BaseMapper<Event> {

}
