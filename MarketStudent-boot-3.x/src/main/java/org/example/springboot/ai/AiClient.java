package org.example.springboot.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.example.springboot.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 大模型客户端：按 OpenAI 兼容协议调用 /chat/completions。
 * <p>
 * 只负责「发请求、取文本」，不掺业务；提示词组装放在 AiService 里。
 */
@Component
public class AiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiClient.class);

    @Resource
    private AiProperties properties;

    /**
     * 单轮对话，返回模型输出的纯文本
     *
     * @param systemPrompt 角色设定
     * @param userPrompt   用户输入
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (!properties.isEnabled()) {
            throw new ServiceException("AI 功能未启用");
        }
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new ServiceException("未配置 AI 密钥，请在 application.properties 中填写 ai.api-key");
        }

        JSONObject payload = new JSONObject();
        payload.set("model", properties.getModel());
        payload.set("temperature", properties.getTemperature());
        payload.set("stream", false);

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
        messages.add(new JSONObject().set("role", "user").set("content", userPrompt));
        payload.set("messages", messages);

        String url = properties.getApiBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        long start = System.currentTimeMillis();

        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey.trim())
                .header("Content-Type", "application/json")
                .body(payload.toString(), "application/json")
                .timeout(properties.getTimeoutMs())
                .execute()) {

            String body = response.body();
            LOGGER.info("调用大模型完成，耗时 {} ms，状态码 {}", System.currentTimeMillis() - start, response.getStatus());

            if (!response.isOk()) {
                String hint = parseError(body);
                LOGGER.error("大模型返回异常状态 {}：{}", response.getStatus(), body);
                throw new ServiceException("AI 服务调用失败：" + hint);
            }

            JSONObject json = JSONUtil.parseObj(body);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new ServiceException("AI 服务未返回内容，请稍后重试");
            }
            String content = choices.getJSONObject(0).getJSONObject("message").getStr("content");
            if (content == null || content.trim().isEmpty()) {
                throw new ServiceException("AI 返回内容为空，请稍后重试");
            }
            return content.trim();

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("调用大模型失败", e);
            throw new ServiceException("AI 服务连接失败，请检查网络或接口地址配置");
        }
    }

    /**
     * 从错误响应里尽量抠出可读的原因
     */
    private String parseError(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "服务无响应";
        }
        try {
            JSONObject json = JSONUtil.parseObj(body);
            Object error = json.get("error");
            if (error instanceof JSONObject errObj && errObj.getStr("message") != null) {
                return errObj.getStr("message");
            }
        } catch (Exception ignored) {
            // 非 JSON 响应，直接截断返回
        }
        return body.length() > 120 ? body.substring(0, 120) + "..." : body;
    }
}
