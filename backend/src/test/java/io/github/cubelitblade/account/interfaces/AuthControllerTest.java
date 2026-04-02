package io.github.cubelitblade.account.interfaces;

import io.github.cubelitblade.account.application.dto.AccountRegisterRequest;
import io.github.cubelitblade.account.application.service.AccountService;
import io.github.cubelitblade.account.domain.exception.UsernameAlreadyExistsException;
import io.github.cubelitblade.account.domain.model.Account;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

@ActiveProfiles("test")
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureRestDocs
class AuthControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    private String serialize(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @Nested
    class register {

        @Test
        void success() {

            AccountRegisterRequest request =
                    new AccountRegisterRequest("user", "password123");

            Account account = mock(Account.class);
            when(account.getId()).thenReturn(1L);

            when(accountService.register(any()))
                    .thenReturn(account);

            assertThat(mvc.post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(serialize(request))
                    .accept(MediaType.APPLICATION_JSON)
            )
                    .hasStatus(HttpStatus.CREATED)
                    .apply(document("auth-register"));
        }

        @Test
        void duplicateUsername() {
            AccountRegisterRequest request = new AccountRegisterRequest(
                    "duplicate",
                    "password123"
            );

            doThrow(new UsernameAlreadyExistsException(request.username()))
                    .when(accountService)
                    .register(request);

            assertThat(mvc.post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(serialize(request))
                    .accept(MediaType.APPLICATION_JSON)
            ).hasStatus(HttpStatus.CONFLICT)
                    .apply(document("auth-register-duplicate-username"));
        }
    }
}