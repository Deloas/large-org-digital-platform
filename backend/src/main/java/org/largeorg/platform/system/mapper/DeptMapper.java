package org.largeorg.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.largeorg.platform.system.entity.SysDept;

@Mapper
public interface DeptMapper extends BaseMapper<SysDept> {
}
