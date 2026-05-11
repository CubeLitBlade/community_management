package io.github.cubelitblade.account.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import io.github.cubelitblade.account.model.Profile;
import io.github.cubelitblade.common.typehandler.InetAddressTypeHandler;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import java.net.InetAddress;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

class AccountDynamicSqlSupportTest {

  @Test
  void should_render_last_login_ip_with_inet_address_type_handler() throws Exception {
    UpdateStatementProvider updateStatement =
        update(AccountDynamicSqlSupport.accounts)
            .set(AccountDynamicSqlSupport.lastLoginIp)
            .equalTo(InetAddress.getByName("127.0.0.1"))
            .where(AccountDynamicSqlSupport.id, isEqualTo(1L))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    assertThat(updateStatement.getUpdateStatement())
        .contains("last_login_ip = ")
        .contains("jdbcType=OTHER")
        .contains("javaType=java.net.InetAddress")
        .contains("typeHandler=" + InetAddressTypeHandler.class.getName());
  }

  @Test
  void should_render_profile_with_jsonb_type_handler() {
    UpdateStatementProvider updateStatement =
        update(AccountDynamicSqlSupport.accounts)
            .set(AccountDynamicSqlSupport.profile)
            .equalTo(new Profile(1))
            .where(AccountDynamicSqlSupport.id, isEqualTo(1L))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    assertThat(updateStatement.getUpdateStatement())
        .contains("profile = ")
        .contains("jdbcType=OTHER")
        .contains("javaType=io.github.cubelitblade.account.model.Profile")
        .contains("typeHandler=" + JsonbTypeHandler.class.getName());
  }
}
