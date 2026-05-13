package org.largeorg.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.largeorg.platform.system.entity.SysUserRole;

@Mapper
public interface UserRoleMapper extends BaseMapper<SysUserRole> {
}
