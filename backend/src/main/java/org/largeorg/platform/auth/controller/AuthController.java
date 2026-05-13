package org.largeorg.platform.auth.controller;

import jakarta.validation.Valid;
import org.largeorg.platform.auth.dto.LoginRequest;
import org.largeorg.platform.auth.service.AuthService;
import org.largeorg.platform.auth.vo.LoginVo;
import org.largeorg.platform.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.success(authService.currentUser());
    }
}
