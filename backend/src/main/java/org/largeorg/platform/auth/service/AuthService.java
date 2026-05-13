package org.largeorg.platform.auth.service;

import org.largeorg.platform.auth.dto.LoginRequest;
import org.largeorg.platform.auth.vo.LoginVo;

import java.util.Map;

public interface AuthService {
    LoginVo login(LoginRequest request);
    void logout();
    Map<String, Object> currentUser();
}
