package com.eva.evagita.controller;

import com.eva.evagita.dto.LoginRequest;
import com.eva.evagita.model.User;
import com.eva.evagita.security.JwtService;
import com.eva.evagita.security.LoginRateLimiter;
import com.eva.evagita.security.SmartCaptchaProperties;
import com.eva.evagita.security.SmartCaptchaVerificationStatus;
import com.eva.evagita.security.SmartCaptchaVerifier;
import com.eva.evagita.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;
    private final SmartCaptchaProperties smartCaptchaProperties;
    private final SmartCaptchaVerifier smartCaptchaVerifier;

    public AuthController(
            AuthenticationService authenticationService,
            JwtService jwtService,
            LoginRateLimiter loginRateLimiter,
            SmartCaptchaProperties smartCaptchaProperties,
            SmartCaptchaVerifier smartCaptchaVerifier
    ) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
        this.smartCaptchaProperties = smartCaptchaProperties;
        this.smartCaptchaVerifier = smartCaptchaVerifier;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = LoginRateLimiter.normalizeEmail(request.getEmail());
        String clientIp = httpRequest.getRemoteAddr();

        boolean captchaRequired = smartCaptchaProperties.isEnabled()
                && loginRateLimiter.requiresCaptcha(email);
        if (captchaRequired && (request.getCaptchaToken() == null
                || request.getCaptchaToken().isBlank())) {
            return ResponseEntity
                    .status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of(
                            "code", "CAPTCHA_REQUIRED",
                            "message", "Additional verification is required"
                    ));
        }

        if (!loginRateLimiter.tryAcquire(email, clientIp)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many login attempts"));
        }

        if (captchaRequired) {
            SmartCaptchaVerificationStatus captchaStatus = smartCaptchaVerifier.verify(
                    request.getCaptchaToken(),
                    clientIp
            );
            if (captchaStatus == SmartCaptchaVerificationStatus.UNAVAILABLE) {
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "code", "CAPTCHA_UNAVAILABLE",
                                "message", "Additional verification is temporarily unavailable"
                        ));
            }
            if (captchaStatus != SmartCaptchaVerificationStatus.VERIFIED) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "code", "CAPTCHA_INVALID",
                                "message", "Additional verification failed"
                        ));
            }
        }

        User user = authenticationService
                .authenticate(email, request.getPassword())
                .orElse(null);

        if (user == null) {
            loginRateLimiter.recordFailure(email, clientIp);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        loginRateLimiter.recordSuccess(email, clientIp);
        String token = jwtService.generateToken(
                user.getUsername(),
                user.getTokenVersion()
        );

        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "token", token
                )
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        authenticationService.revokeTokens(authentication.getName());
    }
}
