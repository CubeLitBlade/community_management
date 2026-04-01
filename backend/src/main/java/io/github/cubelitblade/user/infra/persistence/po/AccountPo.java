package io.github.cubelitblade.user.infra.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.cubelitblade.common.typehandler.InetAddressTypeHandler;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import io.github.cubelitblade.user.domain.model.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.net.InetAddress;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("account")
public class AccountPo {

    @TableId(type = IdType.AUTO)
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
}
