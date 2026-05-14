package org.largeorg.platform.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.largeorg.platform.audit.entity.SecurityAlert;

@Mapper
public interface SecurityAlertMapper extends BaseMapper<SecurityAlert> {
}
