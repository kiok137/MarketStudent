package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_batch_quality")
@Schema(description = "商品批次质量实体")
public class ProductBatchQuality {
    @TableId(value = "quality_id", type = IdType.AUTO)
    @Schema(description = "质量记录ID")
    private Integer qualityId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    @TableField("product_id")
    private Integer productId;

    @NotBlank(message = "批次号不能为空")
    @Schema(description = "批次号")
    @TableField("batch_no")
    private String batchNo;

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID")
    @TableField("supplier_id")
    private Integer supplierId;

    @NotNull(message = "质量等级不能为空")
    @Min(value = 1, message = "质量等级最低为1")
    @Max(value = 4, message = "质量等级最高为4")
    @Schema(description = "质量等级(1-优，2-良，3-中，4-差)")
    @TableField("quality_level")
    private Integer qualityLevel;

    @Schema(description = "质量评价内容")
    private String comment;

    @NotNull(message = "评价用户ID不能为空")
    @Schema(description = "评价用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "商品名称（关联查询字段）")
    private String productName;

    @TableField(exist = false)
    @Schema(description = "供应商名称（关联查询字段）")
    private String supplierName;

    @TableField(exist = false)
    @Schema(description = "评价人（关联查询字段）")
    private String userName;
}
