package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role_menu")
@Schema(description = "角色菜单关联实体")
public class RoleMenu {
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Integer id;
    
    @Schema(description = "角色ID")
    private Integer roleId;
    
    @Schema(description = "菜单ID")
    private Integer menuId;
    
    @Schema(description = "创建时间")
    @TableField("created_time")
    private LocalDateTime createdTime;
} 