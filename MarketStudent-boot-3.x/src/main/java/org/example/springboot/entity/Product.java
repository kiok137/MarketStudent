package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
@Schema(description = "商品实体")
public class Product {
    @TableId(value = "product_id", type = IdType.AUTO)
    @Schema(description = "商品ID")
    private Integer productId;

    @NotBlank(message = "商品名称不能为空")
    @Size(min = 2, max = 100, message = "商品名称长度必须在2到100个字符之间")
    @Schema(description = "商品名称")
    @TableField("product_name")
    private String productName;

    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID")
    @TableField("category_id")
    private Integer categoryId;

    @Schema(description = "商品条码")
    private String barcode;

    @NotBlank(message = "计量单位不能为空")
    @Size(max = 20, message = "计量单位长度不能超过20个字符")
    @Schema(description = "计量单位")
    private String unit;

    @NotNull(message = "零售价格不能为空")
    @Positive(message = "零售价格必须大于0")
    @Schema(description = "零售价格")
    @TableField("retail_price")
    private BigDecimal retailPrice;

    @NotNull(message = "成本价格不能为空")
    @Positive(message = "成本价格必须大于0")
    @Schema(description = "成本价格")
    @TableField("cost_price")
    private BigDecimal costPrice;

    @Schema(description = "商品图片URL")
    @TableField("image_url")
    private String imageUrl;

    @Schema(description = "状态(0-下架，1-上架)")
    private Integer status;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "分类名称（关联查询字段）")
    private String categoryName;

    @TableField(exist = false)
    @Schema(description = "当前库存（关联查询字段）")
    private Integer inventory;
}
