package io.github.cubelitblade.event;

import io.github.cubelitblade.event.payload.DemoEventPayload;
import io.github.cubelitblade.user.domain.model.Role;
import io.github.cubelitblade.user.infra.security.jwt.JwtAuthenticatedUser;
import io.github.cubelitblade.user.infra.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class EventControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private String serialize(Object obj) throws JacksonException {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    void should_return_unauthorized_without_token() throws Exception {
        DemoEventPayload payload = new DemoEventPayload("hello", 1L, 0, true);

        mockMvc.perform(post("/api/event/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serialize(payload))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_allow_request_with_valid_token() throws Exception {
        DemoEventPayload payload = new DemoEventPayload("hello", 1L, 0, true);
        Event event = mock(Event.class);

        when(jwtTokenProvider.parseToken("valid-token"))
                .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));
        when(event.getId()).thenReturn(10L);
        when(eventService.enqueueEvent(eq("demo"), any(DemoEventPayload.class)))
                .thenReturn(event);

        mockMvc.perform(post("/api/event/demo")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serialize(payload))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
