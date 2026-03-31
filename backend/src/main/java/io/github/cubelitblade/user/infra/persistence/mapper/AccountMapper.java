package io.github.cubelitblade.user.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cubelitblade.user.infra.persistence.po.AccountPo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper  extends BaseMapper<AccountPo> {
}
