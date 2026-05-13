package org.largeorg.platform.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.largeorg.platform.audit.entity.AuditOperationLog;

@Mapper
public interface AuditOperationLogMapper extends BaseMapper<AuditOperationLog> {
}
