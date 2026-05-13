package org.largeorg.platform.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.largeorg.platform.audit.entity.AuditLoginLog;
import org.largeorg.platform.audit.service.AuditLoginLogService;
import org.largeorg.platform.audit.util.AuditParamSanitizer;
import org.largeorg.platform.auth.dto.LoginRequest;
import org.largeorg.platform.auth.service.AuthService;
import org.largeorg.platform.auth.vo.LoginVo;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.common.util.IpUtils;
import org.largeorg.platform.system.entity.SysUser;
import org.largeorg.platform.system.mapper.MenuMapper;
import org.largeorg.platform.system.mapper.RoleMapper;
import org.largeorg.platform.system.mapper.UserMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final AuditLoginLogService loginLogService;
    private final HttpServletRequest httpServletRequest;

    public AuthServiceImpl(UserMapper userMapper, RoleMapper roleMapper, MenuMapper menuMapper,
                          AuditLoginLogService loginLogService, HttpServletRequest httpServletRequest) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.loginLogService = loginLogService;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public LoginVo login(LoginRequest request) {
        String username = request.getUsername();
        String ip = IpUtils.getClientIp(httpServletRequest);
        String ua = AuditParamSanitizer.truncate(httpServletRequest.getHeader("User-Agent"), 512);

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            saveLoginLog(null, username, ip, ua, "fail", "用户名不存在");
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            saveLoginLog(user.getId(), username, ip, ua, "fail", "账号已被禁用");
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            saveLoginLog(user.getId(), username, ip, ua, "fail", "密码错误");
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());
        String token = StpUtil.getTokenValue();

        saveLoginLog(user.getId(), username, ip, ua, "success", null);

        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = menuMapper.selectPermissionsByUserId(user.getId());

        return LoginVo.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roles != null ? roles : Collections.emptyList())
                .permissions(permissions != null ? permissions : Collections.emptyList())
                .build();
    }

    private void saveLoginLog(Long userId, String username, String ip, String ua,
                              String status, String failReason) {
        AuditLoginLog log = new AuditLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginIp(ip);
        log.setUserAgent(ua);
        log.setStatus(status);
        log.setFailReason(failReason);
        log.setLoginTime(LocalDateTime.now());
        loginLogService.save(log);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public Map<String, Object> currentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);
        List<String> permissions = menuMapper.selectPermissionsByUserId(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("avatar", user.getAvatar());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("deptId", user.getDeptId());
        result.put("roles", roles != null ? roles : Collections.emptyList());
        result.put("permissions", permissions != null ? permissions : Collections.emptyList());
        return result;
    }
}
