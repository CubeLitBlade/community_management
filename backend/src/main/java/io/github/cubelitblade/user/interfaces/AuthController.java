package io.github.cubelitblade.user.interfaces;

import io.github.cubelitblade.user.application.dto.AccountRegisterRequest;
import io.github.cubelitblade.user.application.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AccountRegisterRequest request) {
        // TODO: Add @Valid annotation for input validation

        Long id = accountService.register(request).getId();
        URI url = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/account/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(url).build();
    }
}
