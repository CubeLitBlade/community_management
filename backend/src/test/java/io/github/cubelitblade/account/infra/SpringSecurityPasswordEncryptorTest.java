package io.github.cubelitblade.account.infra;

import io.github.cubelitblade.account.domain.model.PasswordHash;
import io.github.cubelitblade.account.infra.security.SpringSecurityPasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringSecurityPasswordEncryptorTest {

    private SpringSecurityPasswordService encryptor;

    @BeforeEach
    void setUp() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        encryptor = new SpringSecurityPasswordService(encoder);
    }

    @Test
    void should_encrypt_from_raw_password() {
        String raw = "test_password";

        PasswordHash encrypted = encryptor.fromRaw(raw);

        assertThat(encrypted.value()).isNotEqualTo(raw);
    }

    @Test
    void should_match_for_correct_password() {
        String raw = "test_password";

        PasswordHash encrypted = encryptor.fromRaw(raw);

        assertThat(encryptor.matches(raw, encrypted)).isTrue();
    }

    @Test
    void should_not_match_for_incorrect_password() {
        String raw = "test_password";
        String wrongPassword = "wrong_password";

        PasswordHash encrypted = encryptor.fromRaw(raw);

        assertThat(encryptor.matches(wrongPassword, encrypted)).isFalse();
    }

    @Test
    void should_throw_exception_for_null_or_blank_password() {
        assertThatThrownBy(() -> encryptor.fromRaw(null)).isInstanceOf(IllegalArgumentException.class);
    }
}