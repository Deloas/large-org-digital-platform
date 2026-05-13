package org.largeorg.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.largeorg.platform.system.entity.SysMenu;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.permission IS NOT NULL AND m.permission != '' AND m.status = 1")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.type IN ('directory','menu') " +
            "ORDER BY m.sort_order")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
