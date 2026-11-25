package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("supplier")
@Schema(description = "供应商实体")
public class Supplier {
    @TableId(value = "supplier_id", type = IdType.AUTO)
    @Schema(description = "供应商ID")
    private Integer supplierId;

    @NotBlank(message = "供应商名称不能为空")
    @Schema(description = "供应商名称")
    @TableField("supplier_name")
    private String supplierName;

    @Schema(description = "联系人")
    @TableField("contact_person")
    private String contactPerson;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "电话号码格式不正确")
    @Schema(description = "电话号码")
    private String phone;

    @Pattern(regexp = "^$|^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$",
            message = "电子邮箱格式不正确")
    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "评价列表（关联查询字段）")
    private List<SupplierEvaluation> evaluations;

    @TableField(exist = false)
    @Schema(description = "平均评分（关联查询字段）")
    private Double averageScore;
}
