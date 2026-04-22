package io.github.cubelitblade.account.persistence;

import io.github.cubelitblade.account.model.Profile;
import io.github.cubelitblade.common.typehandler.InetAddressTypeHandler;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;

public interface AccountMapper
    extends CommonCountMapper,
        CommonDeleteMapper,
        CommonInsertMapper<AccountPo>,
        CommonUpdateMapper {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "AccountResult")
  @ConstructorArgs({
    @Arg(column = "id", name = "id", javaType = Long.class, jdbcType = JdbcType.BIGINT, id = true),
    @Arg(
        column = "username",
        name = "username",
        javaType = String.class,
        jdbcType = JdbcType.VARCHAR),
    @Arg(
        column = "password_hash",
        name = "passwordHash",
        javaType = String.class,
        jdbcType = JdbcType.VARCHAR),
    @Arg(
        column = "must_change_password",
        name = "mustChangePassword",
        javaType = Boolean.class,
        jdbcType = JdbcType.BOOLEAN),
    @Arg(
        column = "nickname",
        name = "nickname",
        javaType = String.class,
        jdbcType = JdbcType.VARCHAR),
    @Arg(column = "email", name = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "phone", name = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(
        column = "profile",
        name = "profile",
        javaType = Profile.class,
        jdbcType = JdbcType.OTHER,
        typeHandler = JsonbTypeHandler.class),
    @Arg(column = "role", name = "role", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "status", name = "status", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(
        column = "created_at",
        name = "createdAt",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(
        column = "updated_at",
        name = "updatedAt",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(
        column = "last_login_at",
        name = "lastLoginAt",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(
        column = "last_login_ip",
        name = "lastLoginIp",
        javaType = InetAddress.class,
        jdbcType = JdbcType.OTHER,
        typeHandler = InetAddressTypeHandler.class)
  })
  List<AccountPo> selectMany(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @ResultMap("AccountResult")
  Optional<AccountPo> selectOne(SelectStatementProvider selectStatement);
}
