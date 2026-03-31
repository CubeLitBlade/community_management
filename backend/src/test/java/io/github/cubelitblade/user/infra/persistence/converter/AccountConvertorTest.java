package io.github.cubelitblade.user.infra.persistence.converter;

import io.github.cubelitblade.user.domain.model.*;
import io.github.cubelitblade.user.infra.persistence.po.AccountPo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class AccountConvertorTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T15:30:00Z"), ZoneId.of("UTC"));

    private static final Instant NOW = Instant.now(CLOCK);

    @Nested
    @DisplayName("toPo")
    class ToPoTests {

        @Test
        void shouldConvertAccountToPo() throws Exception {
            // Given
            Username username = Username.reconstitute("cubelitblade");
            Email email = new Email("test@example.com");
            Phone phone = new Phone("13800000000");
            PasswordHash passwordHash = new PasswordHash("hashed_secret");
            Profile profile = new Profile(1);
            InetAddress ip = InetAddress.getByName("192.168.1.100");

            Account.Snapshot snapshot = Account.Snapshot.builder()
                    .id(100L)
                    .username(username)
                    .passwordHash(passwordHash)
                    .nickname("Test Nick")
                    .email(email)
                    .phone(phone)
                    .profile(profile)
                    .role(Role.ADMIN)
                    .status(Status.NORMAL)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .lastLoginAt(NOW)
                    .lastLoginIp(ip)
                    .build();

            Account account = Account.reconstitute(snapshot);

            // When
            AccountPo po = AccountConvertor.toPo(account);

            // Then
            assertThat(po)
                    .extracting(
                            AccountPo::getId,
                            AccountPo::getUsername,
                            AccountPo::getPasswordHash,
                            AccountPo::getNickname,
                            AccountPo::getEmail,
                            AccountPo::getPhone,
                            AccountPo::getRole,
                            AccountPo::getStatus,
                            AccountPo::getLastLoginIp
                    )
                    .containsExactly(
                            100L,
                            "cubelitblade",
                            "hashed_secret",
                            "Test Nick",
                            "test@example.com",
                            "13800000000",
                            "admin",
                            "normal",
                            "192.168.1.100"
                    );
            assertThat(po.getProfile().gender()).isEqualTo(1);
            assertThat(po.getCreatedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomainTests {

        @Test
        void shouldConvertPoToDomain() {

            String ip = "192.168.1.100";

            AccountPo po = AccountPo.builder()
                    .id(10L)
                    .username("domain_user")
                    .passwordHash("db_hash")
                    .nickname("Domain User")
                    .email("domain@test.com")
                    .phone("13900139000")
                    .role("user")
                    .status("suspended")
                    .lastLoginIp(ip)
                    .profile(new Profile(2))
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .build();

            Account account = AccountConvertor.toDomain(po);

            assertThat(account)
                    .extracting(
                            a -> a.getUsername().value(),
                            a -> a.getEmail().value(),
                            Account::getRole,
                            Account::getStatus,
                            a -> a.getLastLoginIp().getHostAddress()
                    )
                    .containsExactly(
                            "domain_user",
                            "domain@test.com",
                            Role.USER,
                            Status.SUSPENDED,
                            ip
                    );
        }

        @Test
        void shouldReturnNullForInvalidIp() {

            AccountPo po = AccountPo.builder()
                    .id(12L)
                    .username("bad_ip_user")
                    .passwordHash("hash")
                    .lastLoginIp("invalid-ip-host")
                    .build();

            Account account = AccountConvertor.toDomain(po);

            assertThat(account.getLastLoginIp()).isNull();
        }
    }
}