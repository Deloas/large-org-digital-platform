package org.largeorg.platform.procurement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.largeorg.platform.procurement.entity.Contract;

@Mapper
public interface ContractMapper extends BaseMapper<Contract> {
}
