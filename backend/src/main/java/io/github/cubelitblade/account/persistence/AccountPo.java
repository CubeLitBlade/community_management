package io.github.cubelitblade.account.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Email;
import io.github.cubelitblade.account.model.PasswordHash;
import io.github.cubelitblade.account.model.Phone;
import io.github.cubelitblade.account.model.Profile;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.common.typehandler.InetAddressTypeHandler;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import java.net.InetAddress;
import java.time.Instant;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("accounts")
public class AccountPo {

  @TableId(type = IdType.INPUT)
  private Long id;

  private String username;
  private String passwordHash;
  private String nickname;
  private String email;
  private String phone;

  @TableField(typeHandler = JsonbTypeHandler.class)
  private Profile profile;

  private String role;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant lastLoginAt;

  @TableField(typeHandler = InetAddressTypeHandler.class, jdbcType = JdbcType.OTHER)
  private InetAddress lastLoginIp;

  public static AccountPo fromDomain(Account account) {
    if (account == null) {
      return null;
    }
    return AccountPo.builder()
        .id(account.getId())
        .username(mapIfNotNull(account.getUsername(), Username::value))
        .passwordHash(mapIfNotNull(account.getPasswordHash(), PasswordHash::value))
        .nickname(account.getNickname())
        .email(mapIfNotNull(account.getEmail(), Email::value))
        .phone(mapIfNotNull(account.getPhone(), Phone::value))
        .profile(account.getProfile())
        .role(mapIfNotNull(account.getRole(), Role::getValue))
        .status(mapIfNotNull(account.getStatus(), Status::getValue))
        .createdAt(account.getCreatedAt())
        .updatedAt(account.getUpdatedAt())
        .lastLoginAt(account.getLastLoginAt())
        .lastLoginIp(account.getLastLoginIp())
        .build();
  }

  public Account toDomain() {
    Account.Snapshot snapshot =
        Account.Snapshot.builder()
            .id(id)
            .username(mapIfNotNull(username, Username::reconstitute))
            .passwordHash(mapIfNotNull(passwordHash, PasswordHash::new))
            .nickname(nickname)
            .email(mapIfNotNull(email, Email::new))
            .phone(mapIfNotNull(phone, Phone::new))
            .profile(profile)
            .role(mapIfNotNull(role, Role::from))
            .status(mapIfNotNull(status, Status::from))
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .lastLoginAt(lastLoginAt)
            .lastLoginIp(lastLoginIp)
            .build();

    return Account.reconstitute(snapshot);
  }

  private static <S, T> T mapIfNotNull(S source, Function<S, T> mapper) {
    if (source == null) {
      return null;
    }
    return mapper.apply(source);
  }
}
