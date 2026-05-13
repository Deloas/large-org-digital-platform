package org.largeorg.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.largeorg.platform.system.entity.SysUser;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
}
