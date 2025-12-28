package com.example.concept_platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.concept_platform.common.Result;
import com.example.concept_platform.entity.Project;
import com.example.concept_platform.entity.Review;
import com.example.concept_platform.entity.vo.ProjectResultVO;
import com.example.concept_platform.service.IProjectService;
import com.example.concept_platform.service.IReviewService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

    @Autowired
    private IReviewService reviewService;

    @Data
    public static class StatItem {
        private String name;
        private Long value;
    }

    // Get List
    @GetMapping("/list")
    public Result<List<Project>> list() {
        return Result.success(projectService.list());
    }

    // Get One by ID
    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Integer id) {
        return Result.success(projectService.getById(id));
    }

    // Add (Simplified, using entity directly)
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Project project) {
        return Result.success(projectService.save(project));
    }

    // Update
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Project project) {
        return Result.success(projectService.updateById(project));
    }

    // Delete
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(projectService.removeById(id));
    }
    
    @GetMapping("/list/my")
    public Result<List<Project>> listMy(@RequestParam Integer applicantId) {
        QueryWrapper<Project> query = new QueryWrapper<>();
        query.eq("applicant_id", applicantId);
        query.orderByDesc("created_at"); // 按时间倒序
        return Result.success(projectService.list(query));
    }

    // Get Project Result
    @GetMapping("/result/{projectId}")
    public Result<ProjectResultVO> getResult(@PathVariable Integer projectId) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            return Result.error("Project not found");
        }

        ProjectResultVO resultVO = new ProjectResultVO();
        resultVO.setProjectId(project.getProjectId());
        resultVO.setProjectName(project.getProjectName());
        resultVO.setStatus(project.getStatus());
        
        // If rejected, set reason
        if (project.getStatus() == 9) {
            resultVO.setRejectReason(project.getRejectReason());
        }

        // Get Reviews
        QueryWrapper<Review> reviewQuery = new QueryWrapper<>();
        reviewQuery.eq("project_id", projectId);
        reviewQuery.eq("status", 1); // Only finished reviews
        List<Review> reviews = reviewService.list(reviewQuery);

        // Convert to SimpleReviewVO (Hide Expert Info)
        List<ProjectResultVO.SimpleReviewVO> simpleReviews = reviews.stream().map(r -> {
            ProjectResultVO.SimpleReviewVO vo = new ProjectResultVO.SimpleReviewVO();
            vo.setExpertName("评审专家"); // Anonymized
            vo.setScore(r.getScore());
            vo.setComments(r.getComments());
            return vo;
        }).collect(Collectors.toList());

        resultVO.setReviews(simpleReviews);
        
        return Result.success(resultVO);
    }

    /**
     * 统计各技术领域项目数量（用于饼图）
     * 支持多选领域，每个领域分别计数
     * 只统计6种标准领域：人工智能、大数据与云计算、信息安全、物联网与嵌入式技术、数字人文技术、其他
     */
    @GetMapping("/stats/domain")
    public Result<List<StatItem>> getDomainStats() {
        List<Project> projects = projectService.list();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Long> domainCountMap = new HashMap<>();
        
        // 初始化6种标准领域
        domainCountMap.put("人工智能", 0L);
        domainCountMap.put("大数据与云计算", 0L);
        domainCountMap.put("信息安全", 0L);
        domainCountMap.put("物联网与嵌入式技术", 0L);
        domainCountMap.put("数字人文技术", 0L);
        domainCountMap.put("其他", 0L);
        
        System.out.println("开始统计技术领域，项目总数: " + projects.size());
        
        for (Project project : projects) {
            String techDomain = project.getTechDomain();
            if (techDomain == null || techDomain.isBlank()) {
                continue;
            }
            
            System.out.println("处理项目: " + project.getProjectName() + ", techDomain: " + techDomain);
            
            List<String> domains = new ArrayList<>();
            try {
                // 尝试解析JSON数组
                domains = objectMapper.readValue(techDomain, new TypeReference<List<String>>() {});
                System.out.println("  JSON解析成功，得到: " + domains);
            } catch (Exception e) {
                // 如果不是JSON格式，尝试按逗号、斜杠、顿号分割（兼容旧数据）
                // 使用正则表达式一次性分割所有分隔符
                String[] parts = techDomain.split("[,/、]");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        domains.add(trimmed);
                    }
                }
                System.out.println("  按分隔符分割，得到: " + domains);
            }
            
            // 标准化每个领域并分别计数
            for (String domain : domains) {
                if (domain == null || domain.trim().isEmpty()) {
                    continue; // 跳过空值
                }
                String normalizedDomain = normalizeDomain(domain.trim());
                System.out.println("    领域: " + domain + " -> 标准化为: " + normalizedDomain);
                domainCountMap.put(normalizedDomain, domainCountMap.get(normalizedDomain) + 1);
            }
        }
        
        System.out.println("统计结果: " + domainCountMap);
        
        // 只返回有数据的领域，按固定顺序
        List<String> standardDomains = Arrays.asList("人工智能", "大数据与云计算", "信息安全", 
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
        
        System.out.println("返回的统计数据: " + stats.stream().map(s -> s.getName() + "=" + s.getValue()).collect(Collectors.joining(", ")));
        
        return Result.success(stats);
    }
    
    /**
     * 标准化领域名称
     * 将各种变体映射到6种标准领域
     */
    private String normalizeDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return "其他";
        }
        
        String normalized = domain.trim();
        
        // 如果还包含逗号或斜杠，说明未正确分割，尝试提取第一个有效部分
        if (normalized.contains(",") || normalized.contains("/") || normalized.contains("、")) {
            // 尝试提取第一个部分
            String[] parts = normalized.split("[,/、]");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    // 递归处理第一个有效部分
                    return normalizeDomain(trimmed);
                }
            }
            return "其他";
        }
        
        // 处理"其他：xxx"格式
        if (normalized.startsWith("其他") || normalized.startsWith("其他：")) {
            return "其他";
        }
        
        // 精确匹配标准领域（优先）
        if (normalized.equals("人工智能")) {
            return "人工智能";
        }
        if (normalized.equals("大数据与云计算")) {
            return "大数据与云计算";
        }
        if (normalized.equals("信息安全")) {
            return "信息安全";
        }
        if (normalized.equals("物联网与嵌入式技术")) {
            return "物联网与嵌入式技术";
        }
        if (normalized.equals("数字人文技术")) {
            return "数字人文技术";
        }
        
        // 模糊匹配（包含关键词）- 注意顺序，更具体的先匹配
        // 先检查包含多个关键词的情况
        if ((normalized.contains("大数据") || normalized.contains("云计算")) 
                && (normalized.contains("人工智能") || normalized.contains("AI"))) {
            // 如果同时包含，需要分别处理，但这里只能返回一个，所以优先返回更匹配的
            // 这种情况应该在上层分割时处理
            return "其他";
        }
        
        // 单个关键词匹配
        if (normalized.contains("人工智能") || normalized.contains("AI") || normalized.contains("机器学习") 
                || normalized.contains("自然语言处理") || normalized.contains("计算机视觉")
                || normalized.contains("深度学习") || normalized.contains("神经网络")) {
            return "人工智能";
        }
        if (normalized.contains("大数据") || normalized.contains("云计算") || normalized.contains("数据治理") 
                || normalized.contains("分布式计算") || normalized.contains("数据挖掘")) {
            return "大数据与云计算";
        }
        if (normalized.contains("信息安全") || normalized.contains("密码") || normalized.contains("数据脱敏") 
                || normalized.contains("漏洞检测") || normalized.contains("网络安全")
                || normalized.contains("加密")) {
            return "信息安全";
        }
        if (normalized.contains("物联网") || normalized.contains("嵌入式")) {
            return "物联网与嵌入式技术";
        }
        if (normalized.contains("数字人文") || normalized.contains("古籍数字化") || normalized.contains("文化遗产")) {
            return "数字人文技术";
        }
        
        // 默认归类为"其他"
        return "其他";
    }

    /**
     * 统计项目状态数量（用于柱状图）
     */
    @GetMapping("/stats/status")
    public Result<List<StatItem>> getStatusStats() {
        List<Project> projects = projectService.list();
        List<StatItem> stats = projects.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus() == null ? -1 : p.getStatus(),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> {
                    StatItem item = new StatItem();
                    String name;
                    switch (e.getKey()) {
                        case 1: name = "待审核"; break;
                        case 2: name = "评审中"; break;
                        case 3: name = "已入库"; break;
                        case 9: name = "已驳回"; break;
                        case 0: name = "草稿"; break;
                        default: name = "未知"; break;
                    }
                    item.setName(name);
                    item.setValue(e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        return Result.success(stats);
    }

    // Assign Experts
    @PostMapping("/assign")
    public Result<Boolean> assign(@RequestBody AssignDto assignDto) {
        if (assignDto.getProjectId() == null || assignDto.getExpertIds() == null || assignDto.getExpertIds().isEmpty()) {
            return Result.error("Project ID and Expert IDs are required");
        }

        // 1. Update Project Status to 2 (Reviewing)
        Project project = new Project();
        project.setProjectId(assignDto.getProjectId());
        project.setStatus(2); 
        projectService.updateById(project);

        // 2. Create Review Records
        List<Review> reviews = new ArrayList<>();
        for (Integer expertId : assignDto.getExpertIds()) {
            Review review = new Review();
            review.setProjectId(assignDto.getProjectId());
            review.setExpertId(expertId);
            review.setStatus(0); // Not reviewed yet
            reviews.add(review);
        }
        
        return Result.success(reviewService.saveBatch(reviews));
    }

    // Audit Project (New)
    @PostMapping("/audit")
    public Result<Boolean> audit(@RequestBody AuditDto auditDto) {
        if (auditDto.getProjectId() == null || auditDto.getPass() == null) {
            return Result.error("Project ID and Pass status are required");
        }

        Project project = new Project();
        project.setProjectId(auditDto.getProjectId());
        project.setAuditTime(LocalDateTime.now()); // Set Audit Time

        if (auditDto.getPass()) {
            // Check if experts are assigned
            if (auditDto.getExpertIds() == null || auditDto.getExpertIds().isEmpty()) {
                return Result.error("请至少选择一名专家");
            }
            project.setStatus(2); // Passed initial review, ready for expert review
            projectService.updateById(project);

            // Assign experts
            List<Review> reviews = new ArrayList<>();
            for (Integer expertId : auditDto.getExpertIds()) {
                Review review = new Review();
                review.setProjectId(auditDto.getProjectId());
                review.setExpertId(expertId);
                review.setStatus(0); // Not reviewed yet
                reviews.add(review);
            }
            reviewService.saveBatch(reviews);
        } else {
            project.setStatus(9); // Rejected
            project.setRejectReason(auditDto.getRejectReason());
            projectService.updateById(project);
        }

        return Result.success(true);
    }

    @Data
    public static class AssignDto {
        private Integer projectId;
        private List<Integer> expertIds;
    }

    @Data
    public static class AuditDto {
        private Integer projectId;
        private Boolean pass;
        private String rejectReason;
        private List<Integer> expertIds;
    }
}
