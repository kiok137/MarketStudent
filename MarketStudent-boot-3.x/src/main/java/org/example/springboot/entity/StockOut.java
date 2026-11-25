package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock_out")
@Schema(description = "出库记录实体")
public class StockOut {
    @TableId(value = "stock_out_id", type = IdType.AUTO)
    @Schema(description = "出库ID")
    private Integer stockOutId;

    @NotBlank(message = "出库单号不能为空")
    @Schema(description = "出库单号")
    @TableField("stock_out_no")
    private String stockOutNo;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    @TableField("product_id")
    private Integer productId;

    @NotNull(message = "出库数量不能为空")
    @Positive(message = "出库数量必须大于0")
    @Schema(description = "出库数量")
    private Integer quantity;

    @Schema(description = "批次号")
    @TableField("batch_no")
    private String batchNo;

    @NotNull(message = "操作用户ID不能为空")
    @Schema(description = "操作用户ID")
    @TableField("user_id")
    private Integer userId;

    @NotBlank(message = "领用部门不能为空")
    @Schema(description = "领用部门")
    private String department;

    @Schema(description = "备注")
    private String remark;

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
    @Schema(description = "操作人（关联查询字段）")
    private String username;
}
