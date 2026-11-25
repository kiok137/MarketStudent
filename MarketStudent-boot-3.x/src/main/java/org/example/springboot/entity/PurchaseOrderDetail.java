package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_order_detail")
@Schema(description = "采购订单详情实体")
public class PurchaseOrderDetail {
    @TableId(value = "detail_id", type = IdType.AUTO)
    @Schema(description = "详情ID")
    private Integer detailId;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    @TableField("order_id")
    private Integer orderId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    @TableField("product_id")
    private Integer productId;

    @NotNull(message = "采购数量不能为空")
    @Positive(message = "采购数量必须大于0")
    @Schema(description = "采购数量")
    private Integer quantity;

    @NotNull(message = "采购单价不能为空")
    @Positive(message = "采购单价必须大于0")
    @Schema(description = "采购单价")
    private BigDecimal price;

    @NotNull(message = "采购金额不能为空")
    @Positive(message = "采购金额必须大于0")
    @Schema(description = "采购金额")
    private BigDecimal amount;

    @Schema(description = "已收货数量")
    @TableField("received_quantity")
    private Integer receivedQuantity;

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
    @Schema(description = "计量单位（关联查询字段）")
    private String productUnit;
}
