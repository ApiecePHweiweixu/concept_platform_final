package com.example.concept_platform.service.impl;

import com.example.concept_platform.entity.Project;
import com.example.concept_platform.entity.ProjectAiAnalysis;
import com.example.concept_platform.service.IAIService;
import com.example.concept_platform.service.IProjectAiAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIServiceImpl implements IAIService {

    @Value("${zhipu.ai.api-key}")
    private String apiKey;

    @Value("${zhipu.ai.api-url}")
    private String apiUrl;

    @Value("${zhipu.ai.model}")
    private String model;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IProjectAiAnalysisService aiAnalysisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Async
    @Override
    public void analyzeProject(Project project) {
        log.info("Starting AI analysis for project: {}", project.getProjectName());

        // 1. Prepare Record
        ProjectAiAnalysis analysis = new ProjectAiAnalysis();
        analysis.setProjectId(project.getProjectId());
        analysis.setStatus(0); // In Progress
        aiAnalysisService.save(analysis);

        try {
            // 2. Prepare Prompt
            String systemPrompt = "你是一位资深的科技成果转化专家和风险投资人。你擅长从技术创新性、商业化前景和落地可行性三个维度客观评估早期科研项目。";
            String userPrompt = String.format(
                "请阅读以下科技项目申报书：\n" +
                "【项目名称】：%s\n" +
                "【项目简介】：%s\n" +
                "【应用场景】：%s\n" +
                "【预期经费】：%s万元\n\n" +
                "请基于以上信息进行分析，严格按照以下 JSON 格式返回结果（不要包含任何多余文字或Markdown代码块标签，直接返回JSON字符串）：\n" +
                "{\n" +
                "  \"innovation\": 85, // 创新性评分(0-100)\n" +
                "  \"feasibility\": 70, // 技术可行性评分(0-100)\n" +
                "  \"market\": 90, // 市场潜力评分(0-100)\n" +
                "  \"summary\": \"一句话总结项目核心价值。\",\n" +
                "  \"risks\": \"指出一到两个核心风险点。\"\n" +
                "}",
                project.getProjectName(),
                project.getDescription(),
                project.getApplicationScenario(),
                project.getBudget()
            );

            // 3. Prepare API Request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            requestBody.put("messages", messages);
            
            // GLM-4.7 specific settings from user example
            requestBody.put("max_tokens", 65536);
            requestBody.put("temperature", 1.0);
            requestBody.put("thinking", Map.of("type", "enabled"));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 4. Call API
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            log.debug("AI Raw Response: {}", response);

            // 5. Parse Response
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            // Clean content if it contains markdown code blocks
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7);
                content = content.substring(0, content.lastIndexOf("```"));
            } else if (content.contains("```")) {
                content = content.substring(content.indexOf("```") + 3);
                content = content.substring(0, content.lastIndexOf("```"));
            }
            content = content.trim();

            JsonNode resultJson = objectMapper.readTree(content);
            
            analysis.setInnovationScore(resultJson.path("innovation").asInt());
            analysis.setFeasibilityScore(resultJson.path("feasibility").asInt());
            analysis.setMarketScore(resultJson.path("market").asInt());
            analysis.setAnalysisSummary(resultJson.path("summary").asText());
            analysis.setRiskWarning(resultJson.path("risks").asText());
            analysis.setRawResponse(response);
            analysis.setStatus(1); // Success
            
        } catch (Exception e) {
            log.error("AI Analysis failed for project " + project.getProjectId(), e);
            analysis.setStatus(2); // Failed
            analysis.setAnalysisSummary("AI分析失败：" + e.getMessage());
        }

        aiAnalysisService.updateById(analysis);
    }
}

