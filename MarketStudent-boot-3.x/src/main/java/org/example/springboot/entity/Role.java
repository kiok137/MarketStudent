package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("role")
@Schema(description = "角色实体")
public class Role {
    @TableId(value = "role_id", type = IdType.AUTO)
    @Schema(description = "角色ID")
    private Integer roleId;
    
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在2到50个字符之间")
    @Schema(description = "角色名称")
    @TableField("role_name")
    private String roleName;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 非数据库字段
    @TableField(exist = false)
    private List<Menu> menuList;
} 