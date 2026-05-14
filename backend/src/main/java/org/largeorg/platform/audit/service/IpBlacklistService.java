package org.largeorg.platform.audit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.dto.BlacklistQueryRequest;
import org.largeorg.platform.audit.dto.BlacklistRequest;
import org.largeorg.platform.audit.vo.IpBlacklistVo;

public interface IpBlacklistService {
    Page<IpBlacklistVo> pageBlacklist(BlacklistQueryRequest request);
    void addBlacklist(BlacklistRequest request, String createdBy);
    void updateBlacklist(Long id, BlacklistRequest request);
    void deleteBlacklist(Long id);
}
