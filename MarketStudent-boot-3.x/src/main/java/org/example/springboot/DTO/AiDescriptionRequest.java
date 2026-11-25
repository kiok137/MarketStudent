package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品文案生成请求
 */
@Data
@Schema(description = "AI 商品文案生成请求")
public class AiDescriptionRequest {
    @Schema(description = "商品名称", example = "农夫山泉饮用天然水")
    private String productName;

    @Schema(description = "商品分类", example = "饮用水")
    private String categoryName;

    @Schema(description = "卖点关键词，逗号分隔", example = "550ml,整箱24瓶,家庭装")
    private String features;

    @Schema(description = "文案风格：简洁 / 活泼 / 促销", example = "简洁")
    private String tone;

    @Schema(description = "期望字数，默认 120")
    private Integer wordLimit;
}
