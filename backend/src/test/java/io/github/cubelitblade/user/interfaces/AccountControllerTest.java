package io.github.cubelitblade.user.interfaces;

import io.github.cubelitblade.user.application.service.AccountService;
import io.github.cubelitblade.user.domain.model.Account;
import io.github.cubelitblade.user.domain.model.Role;
import io.github.cubelitblade.user.domain.model.Status;
import io.github.cubelitblade.user.domain.model.Username;
import io.github.cubelitblade.user.infra.security.jwt.JwtAuthenticatedUser;
import io.github.cubelitblade.user.infra.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class AccountControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void should_return_unauthorized_without_token() {
        assertThat(mvc.get()
                .uri("/api/account/me")
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .apply(document("account-me-unauthorized"));
    }

    @Test
    void should_return_me_with_valid_token() {
        Account account = Account.reconstitute(Account.Snapshot.builder()
                .id(1L)
                .username(Username.of("Alice"))
                .nickname("Alice")
                .role(Role.USER)
                .status(Status.NORMAL)
                .build());

        when(accountService.findAccount(1L)).thenReturn(Optional.of(account));
        when(jwtTokenProvider.parseToken("valid-token"))
                .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));

        assertThat(mvc.get()
                .uri("/api/account/me")
                .header("Authorization", "Bearer valid-token")
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.OK)
                .apply(document("account-me"))
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(1);
                    assertThat(json).extractingPath("$.username").isEqualTo("Alice");
                    assertThat(json).extractingPath("$.nickname").isEqualTo("Alice");
                    assertThat(json).extractingPath("$.role").isEqualTo("user");
                    assertThat(json).extractingPath("$.status").isEqualTo("normal");
                });
    }
}
