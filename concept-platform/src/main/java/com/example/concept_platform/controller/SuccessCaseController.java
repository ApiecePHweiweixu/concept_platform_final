package com.example.concept_platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.concept_platform.common.Result;
import com.example.concept_platform.entity.IncubationProject;
import com.example.concept_platform.entity.Project;
import com.example.concept_platform.entity.SuccessCase;
import com.example.concept_platform.service.IIncubationProjectService;
import com.example.concept_platform.service.IProjectService;
import com.example.concept_platform.service.ISuccessCaseService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成功案例/公共展示招标界面控制器
 */
@RestController
@RequestMapping("/api/success-case")
public class SuccessCaseController {

    @Autowired
    private ISuccessCaseService successCaseService;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private IIncubationProjectService incubationProjectService;

    /**
     * 管理员：将已毕业的孵化项目转为成功案例
     */
    @PostMapping("/create")
    public Result<Boolean> createSuccessCase(@RequestBody CreateCaseDto dto) {
        if (dto.getIncubationId() == null) {
            return Result.error("孵化项目ID不能为空");
        }
        IncubationProject ip = incubationProjectService.getById(dto.getIncubationId());
        if (ip == null || ip.getStatus() != 2) { // 必须是已毕业
            return Result.error("只有已毕业的孵化项目才能转为成功案例");
        }
        Project p = projectService.getById(ip.getProjectId());
        if (p == null) {
            return Result.error("项目不存在");
        }

        // 检查是否已存在案例
        QueryWrapper<SuccessCase> query = new QueryWrapper<>();
        query.eq("incubation_id", dto.getIncubationId());
        SuccessCase existing = successCaseService.getOne(query);
        if (existing != null) {
            return Result.error("该孵化项目已存在成功案例");
        }

        SuccessCase successCase = new SuccessCase();
        successCase.setProjectId(p.getProjectId());
        successCase.setIncubationId(dto.getIncubationId());
        successCase.setCaseName(dto.getCaseName() != null ? dto.getCaseName() : p.getProjectName());
        successCase.setCaseDescription(dto.getCaseDescription() != null ? dto.getCaseDescription() : p.getDescription());
        successCase.setTechDomain(p.getTechDomain());
        successCase.setApplicationScenario(p.getApplicationScenario());
        successCase.setIntellectualProperty(p.getIntellectualProperty());
        successCase.setCooperationMode(p.getCooperationNeed());
        successCase.setContactInfo(dto.getContactInfo());
        successCase.setDisplayStatus(1); // 公开展示
        successCase.setViewCount(0);
        successCaseService.save(successCase);
        return Result.success(true);
    }

    /**
     * 公共展示：获取所有成功案例列表
     */
    @GetMapping("/public/list")
    public Result<List<SuccessCase>> publicList() {
        QueryWrapper<SuccessCase> query = new QueryWrapper<>();
        query.eq("display_status", 1);
        query.orderByDesc("created_at");
        return Result.success(successCaseService.list(query));
    }

    /**
     * 公共展示：获取案例详情（增加浏览次数）
     */
    @GetMapping("/public/{caseId}")
    public Result<SuccessCase> getCaseDetail(@PathVariable Integer caseId) {
        SuccessCase successCase = successCaseService.getById(caseId);
        if (successCase == null || successCase.getDisplayStatus() != 1) {
            return Result.error("案例不存在或未公开");
        }
        // 增加浏览次数
        successCase.setViewCount((successCase.getViewCount() != null ? successCase.getViewCount() : 0) + 1);
        successCaseService.updateById(successCase);
        return Result.success(successCase);
    }

    /**
     * 统计：成功案例技术领域分布（用于饼图）
     */
    @GetMapping("/stats/domain")
    public Result<List<StatItem>> getDomainStats() {
        QueryWrapper<SuccessCase> query = new QueryWrapper<>();
        query.eq("display_status", 1);
        List<SuccessCase> cases = successCaseService.list(query);
        
        Map<String, Long> domainCountMap = new HashMap<>();
        domainCountMap.put("人工智能", 0L);
        domainCountMap.put("大数据与云计算", 0L);
        domainCountMap.put("信息安全", 0L);
        domainCountMap.put("物联网与嵌入式技术", 0L);
        domainCountMap.put("数字人文技术", 0L);
        domainCountMap.put("其他", 0L);
        
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        for (SuccessCase sc : cases) {
            if (sc.getTechDomain() == null) continue;
            try {
                List<String> domains = mapper.readValue(
                        sc.getTechDomain(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                for (String domain : domains) {
                    String normalized = normalizeDomain(domain);
                    domainCountMap.put(normalized, domainCountMap.get(normalized) + 1);
                }
            } catch (Exception e) {
                String normalized = normalizeDomain(sc.getTechDomain());
                domainCountMap.put(normalized, domainCountMap.get(normalized) + 1);
            }
        }
        
        List<String> standardDomains = List.of("人工智能", "大数据与云计算", "信息安全", 
                "物联网与嵌入式技术", "数字人文技术", "其他");
        List<StatItem> stats = standardDomains.stream()
                .filter(domain -> domainCountMap.get(domain) > 0)
                .map(domain -> {
                    StatItem item = new StatItem();
                    item.setName(domain);
                    item.setValue(domainCountMap.get(domain));
                    return item;
                })
                .collect(Collectors.toList());
        
        return Result.success(stats);
    }

    /**
     * 统计：成功案例数量趋势（用于柱状图）
     */
    @GetMapping("/stats/trend")
    public Result<List<StatItem>> getTrendStats() {
        QueryWrapper<SuccessCase> query = new QueryWrapper<>();
        query.eq("display_status", 1);
        query.orderByAsc("created_at");
        List<SuccessCase> cases = successCaseService.list(query);
        
        // 按年份统计
        Map<String, Long> yearMap = new HashMap<>();
        for (SuccessCase sc : cases) {
            if (sc.getCreatedAt() != null) {
                String year = String.valueOf(sc.getCreatedAt().getYear());
                yearMap.put(year, yearMap.getOrDefault(year, 0L) + 1);
            }
        }
        
        List<StatItem> stats = yearMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    StatItem item = new StatItem();
                    item.setName(e.getKey() + "年");
                    item.setValue(e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        return Result.success(stats);
    }

    private String normalizeDomain(String domain) {
        if (domain == null || domain.isBlank()) return "其他";
        String normalized = domain.trim();
        if (normalized.contains("人工智能") || normalized.contains("AI")) return "人工智能";
        if (normalized.contains("大数据") || normalized.contains("云计算")) return "大数据与云计算";
        if (normalized.contains("信息安全") || normalized.contains("密码")) return "信息安全";
        if (normalized.contains("物联网") || normalized.contains("嵌入式")) return "物联网与嵌入式技术";
        if (normalized.contains("数字人文") || normalized.contains("古籍")) return "数字人文技术";
        return "其他";
    }

    @Data
    public static class CreateCaseDto {
        private Integer incubationId;
        private String caseName;
        private String caseDescription;
        private String contactInfo;
    }

    @Data
    public static class StatItem {
        private String name;
        private Long value;
    }
}

