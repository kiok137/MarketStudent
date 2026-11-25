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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("purchase_order")
@Schema(description = "采购订单实体")
public class PurchaseOrder {
    @TableId(value = "order_id", type = IdType.AUTO)
    @Schema(description = "订单ID")
    private Integer orderId;

    @NotBlank(message = "订单编号不能为空")
    @Schema(description = "订单编号")
    @TableField("order_no")
    private String orderNo;

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID")
    @TableField("supplier_id")
    private Integer supplierId;

    @Schema(description = "下单用户ID")
    @TableField("user_id")
    private Integer userId;

    @NotNull(message = "订单总金额不能为空")
    @Positive(message = "订单总金额必须大于0")
    @Schema(description = "订单总金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态(0-待审核,1-已审核,2-已收货,3-已取消)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "审核时间")
    @TableField("approved_at")
    private LocalDateTime approvedAt;

    @Schema(description = "收货时间")
    @TableField("received_at")
    private LocalDateTime receivedAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "供应商名称（关联查询字段）")
    private String supplierName;

    @TableField(exist = false)
    @Schema(description = "下单人姓名（关联查询字段）")
    private String username;

    @TableField(exist = false)
    @Schema(description = "订单明细（关联查询字段）")
    private List<PurchaseOrderDetail> detailList;
}
