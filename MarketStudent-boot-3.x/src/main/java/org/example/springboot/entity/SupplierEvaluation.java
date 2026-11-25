package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("supplier_evaluation")
@Schema(description = "供应商评价实体")
public class SupplierEvaluation {
    @TableId(value = "evaluation_id", type = IdType.AUTO)
    @Schema(description = "评价ID")
    private Integer evaluationId;

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID")
    @TableField("supplier_id")
    private Integer supplierId;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    @TableField("order_id")
    private Integer orderId;

    @NotNull(message = "配送速度评分不能为空")
    @Min(value = 1, message = "配送速度评分最低为1")
    @Max(value = 5, message = "配送速度评分最高为5")
    @Schema(description = "配送速度评分(1-5)")
    @TableField("delivery_speed_score")
    private Integer deliverySpeedScore;

    @NotNull(message = "商品质量评分不能为空")
    @Min(value = 1, message = "商品质量评分最低为1")
    @Max(value = 5, message = "商品质量评分最高为5")
    @Schema(description = "商品质量评分(1-5)")
    @TableField("quality_score")
    private Integer qualityScore;

    @NotNull(message = "服务态度评分不能为空")
    @Min(value = 1, message = "服务态度评分最低为1")
    @Max(value = 5, message = "服务态度评分最高为5")
    @Schema(description = "服务态度评分(1-5)")
    @TableField("service_score")
    private Integer serviceScore;

    @NotNull(message = "价格评分不能为空")
    @Min(value = 1, message = "价格评分最低为1")
    @Max(value = 5, message = "价格评分最高为5")
    @Schema(description = "价格评分(1-5)")
    @TableField("price_score")
    private Integer priceScore;

    @Schema(description = "总评分(四项之和，4-20)")
    @TableField("total_score")
    private Integer totalScore;

    @Schema(description = "评价内容")
    private String comment;

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
    @Schema(description = "供应商名称（关联查询字段）")
    private String supplierName;

    @TableField(exist = false)
    @Schema(description = "采购单号（关联查询字段）")
    private String orderNo;
}
