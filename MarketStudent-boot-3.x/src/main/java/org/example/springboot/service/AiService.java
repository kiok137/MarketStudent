package org.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.AiDescriptionRequest;
import org.example.springboot.ai.AiClient;
import org.example.springboot.entity.Inventory;
import org.example.springboot.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 业务层：负责组装提示词、约束输出格式，把大模型能力包装成业务接口。
 */
@Service
public class AiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiService.class);

    private static final String DESCRIPTION_SYSTEM_PROMPT = """
            你是一名超市商品运营的文案编辑。请根据用户提供的商品信息，写一段中文商品描述。
            要求：
            1. 只输出正文，不要标题、不要引号、不要 Markdown 标记；
            2. 语言口语化、具体，突出使用场景和人群；
            3. 不使用「最好」「第一」「100%」「国家级」等绝对化或违反广告法的表述；
            4. 不编造商品本身没有的功效、成分或认证；
            5. 控制在用户要求的字数范围内。
            """;

    private static final String STOCK_SYSTEM_PROMPT = """
            你是一名超市库存管理助手。用户会给出一批库存低于安全线的商品清单。
            请输出补货建议：
            1. 先用一句话概括整体库存风险；
            2. 再按紧急程度从高到低给出 3-5 条具体建议，每条包含商品名称与建议补货量区间；
            3. 只输出正文，不要 Markdown 标题和代码块，不要编造清单以外的商品。
            """;

    @Resource
    private AiClient aiClient;

    @Resource
    private InventoryService inventoryService;

    /**
     * 生成商品描述
     */
    public String generateProductDescription(AiDescriptionRequest request) {
        if (request == null || request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new ServiceException("商品名称不能为空");
        }
        int wordLimit = request.getWordLimit() == null ? 120
                : Math.min(Math.max(request.getWordLimit(), 50), 300);
        String tone = request.getTone() == null || request.getTone().trim().isEmpty()
                ? "简洁" : request.getTone().trim();

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("商品名称：").append(request.getProductName().trim()).append('\n');
        if (request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty()) {
            userPrompt.append("商品分类：").append(request.getCategoryName().trim()).append('\n');
        }
        if (request.getFeatures() != null && !request.getFeatures().trim().isEmpty()) {
            userPrompt.append("卖点关键词：").append(request.getFeatures().trim()).append('\n');
        }
        userPrompt.append("文案风格：").append(tone).append('\n');
        userPrompt.append("字数要求：").append(wordLimit).append(" 字左右\n");
        userPrompt.append("请直接输出商品描述正文。");

        String content = aiClient.chat(DESCRIPTION_SYSTEM_PROMPT, userPrompt.toString());
        return cleanContent(content);
    }

    /**
     * 基于当前低库存商品生成补货建议
     */
    public Map<String, Object> generateStockAdvice() {
        Page<Inventory> page = inventoryService.getInventoryByPage(null, true, 1, 20);
        List<Inventory> lowStock = page.getRecords();

        Map<String, Object> result = new HashMap<>();
        result.put("count", lowStock.size());

        List<Map<String, Object>> items = new ArrayList<>();
        for (Inventory inventory : lowStock) {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", inventory.getProductName());
            item.put("quantity", inventory.getQuantity());
            item.put("minQuantity", inventory.getMinQuantity());
            items.add(item);
        }
        result.put("items", items);

        if (lowStock.isEmpty()) {
            result.put("advice", "当前没有低于安全库存的商品，库存水位正常。");
            return result;
        }

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("统计日期：").append(LocalDate.now()).append('\n');
        userPrompt.append("低库存商品清单（商品名称 / 当前库存 / 最低库存）：\n");
        for (Inventory inventory : lowStock) {
            userPrompt.append("- ").append(inventory.getProductName())
                    .append(" / 当前 ").append(inventory.getQuantity())
                    .append(" / 最低 ").append(inventory.getMinQuantity())
                    .append('\n');
        }
        userPrompt.append("请给出补货建议。");

        String advice = aiClient.chat(STOCK_SYSTEM_PROMPT, userPrompt.toString());
        result.put("advice", cleanContent(advice));
        return result;
    }

    /**
     * 去掉模型偶尔带出的引号、前缀和多余空行
     */
    private String cleanContent(String content) {
        if (content == null) {
            return "";
        }
        String text = content.trim();
        text = text.replaceAll("^[\"'“”「」]+", "").replaceAll("[\"'“”「」]+$", "");
        text = text.replaceAll("^(商品描述|描述|建议)[:：]\\s*", "");
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }
}
