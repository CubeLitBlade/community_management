package io.github.cubelitblade.activity.persistence;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;

public interface ActivityRegistrationMapper
    extends CommonCountMapper, CommonInsertMapper<ActivityRegistrationPo> {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "ActivityRegistrationResult")
  @ConstructorArgs({
    @Arg(column = "activity_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(
        column = "created_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE)
  })
  List<ActivityRegistrationPo> selectMany(SelectStatementProvider selectStatement);

  @DeleteProvider(type = SqlProviderAdapter.class, method = "delete")
  int delete(DeleteStatementProvider deleteStatement);
}
