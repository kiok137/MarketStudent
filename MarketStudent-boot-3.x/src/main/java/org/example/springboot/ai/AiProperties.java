package org.example.springboot.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型服务配置，对应 application.properties 中的 ai.* 配置项。
 * 默认对接 DeepSeek，任何兼容 OpenAI 协议的接口都可以直接替换。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    /** 是否启用 AI 能力 */
    private boolean enabled = true;

    /** 接口地址（OpenAI 兼容，不带 /chat/completions） */
    private String apiBaseUrl = "https://api.deepseek.com/v1";

    /** 密钥，留空则调用时返回友好提示 */
    private String apiKey = "";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 超时时间（毫秒） */
    private int timeoutMs = 20000;

    /** 采样温度，文案类任务建议 0.7 */
    private double temperature = 0.7;
}
