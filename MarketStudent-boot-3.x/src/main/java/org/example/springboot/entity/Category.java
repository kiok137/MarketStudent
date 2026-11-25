package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("category")
@Schema(description = "商品分类实体")
public class Category {
    @TableId(value = "category_id",type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Integer categoryId;

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2,max = 50,message = "分类名称长度必须在2到50个字符之间")
    @Schema(description = "分类名称")
    @TableField("category_name")
    private String categoryName;

    @Schema(description = "父分类ID")
    @TableField(condition = "parent_id",updateStrategy = FieldStrategy.IGNORED)
    private Integer parentId;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist =false)
    private List<Category> children;

    @TableField(exist =false)
    private String parentName;
}
