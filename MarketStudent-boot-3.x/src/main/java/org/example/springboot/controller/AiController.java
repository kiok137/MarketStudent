package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.AiDescriptionRequest;
import org.example.springboot.ai.AiProperties;
import org.example.springboot.common.Result;
import org.example.springboot.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "AI 能力接口")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    @Resource
    private AiProperties aiProperties;

    @Operation(summary = "AI 服务状态")
    @GetMapping("/status")
    public Result<?> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", aiProperties.isEnabled());
        status.put("configured", aiProperties.getApiKey() != null && !aiProperties.getApiKey().trim().isEmpty());
        status.put("model", aiProperties.getModel());
        return Result.success(status);
    }

    @Operation(summary = "AI 生成商品描述")
    @PostMapping("/product-description")
    public Result<?> generateDescription(@RequestBody AiDescriptionRequest request) {
        String description = aiService.generateProductDescription(request);
        Map<String, Object> data = new HashMap<>();
        data.put("description", description);
        return Result.success(data);
    }

    @Operation(summary = "AI 生成库存补货建议")
    @GetMapping("/stock-advice")
    public Result<?> generateStockAdvice() {
        return Result.success(aiService.generateStockAdvice());
    }
}
