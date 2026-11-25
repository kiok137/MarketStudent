package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory")
@Schema(description = "库存实体")
public class Inventory {
    @TableId(value = "inventory_id", type = IdType.AUTO)
    @Schema(description = "库存ID")
    private Integer inventoryId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    @TableField("product_id")
    private Integer productId;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能小于0")
    @Schema(description = "库存数量")
    private Integer quantity;

    @NotNull(message = "最低库存数量不能为空")
    @Min(value = 0, message = "最低库存数量不能小于0")
    @Schema(description = "最低库存数量")
    @TableField("min_quantity")
    private Integer minQuantity;

    @NotNull(message = "最高库存数量不能为空")
    @Min(value = 0, message = "最高库存数量必须大于等于0")
    @Schema(description = "最高库存数量")
    @TableField("max_quantity")
    private Integer maxQuantity;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "商品名称（关联查询字段）")
    private String productName;

    @TableField(exist = false)
    @Schema(description = "商品条码（关联查询字段）")
    private String barcode;

    @TableField(exist = false)
    @Schema(description = "计量单位（关联查询字段）")
    private String unit;
}
