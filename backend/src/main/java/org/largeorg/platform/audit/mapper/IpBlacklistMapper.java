package org.largeorg.platform.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.largeorg.platform.audit.entity.IpBlacklist;

@Mapper
public interface IpBlacklistMapper extends BaseMapper<IpBlacklist> {
}
