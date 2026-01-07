package com.example.concept_platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.concept_platform.common.Result;
import com.example.concept_platform.entity.IncubationMilestone;
import com.example.concept_platform.entity.IncubationMilestoneReport;
import com.example.concept_platform.entity.IncubationProject;
import com.example.concept_platform.entity.IncubationResourceRequest;
import com.example.concept_platform.entity.Project;
import com.example.concept_platform.entity.SuccessCase;
import com.example.concept_platform.service.IIncubationMilestoneReportService;
import com.example.concept_platform.service.IIncubationMilestoneService;
import com.example.concept_platform.service.IIncubationProjectService;
import com.example.concept_platform.service.IIncubationResourceRequestService;
import com.example.concept_platform.service.IProjectService;
import com.example.concept_platform.service.ISuccessCaseService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 孵化阶段相关接口：
 * - 创建/查看孵化项目工作台
 * - 入孵协议与信息表
 * - 里程碑管理与进展报告
 * - 资源申请
 * - 毕业申请
 */
@RestController
@RequestMapping("/api/incubation")
public class IncubationController {

    @Autowired
    private IIncubationProjectService incubationProjectService;

    @Autowired
    private IIncubationMilestoneService incubationMilestoneService;

    @Autowired
    private IIncubationMilestoneReportService milestoneReportService;

    @Autowired
    private IIncubationResourceRequestService resourceRequestService;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private ISuccessCaseService successCaseService;

    @Autowired
    private com.example.concept_platform.service.ISysUserService sysUserService;

    /**
     * 管理员：查看所有孵化项目（含项目名称与状态）
     */
    @GetMapping("/admin/list")
    public Result<List<IncubationAdminVO>> adminList() {
        List<IncubationProject> incubations = incubationProjectService.list();
        List<IncubationAdminVO> vos = incubations.stream().map(ip -> {
            IncubationAdminVO vo = new IncubationAdminVO();
            vo.setIncubationId(ip.getIncubationId());
            vo.setProjectId(ip.getProjectId());
            vo.setStatus(ip.getStatus());
            vo.setProjectManagerId(ip.getProjectManagerId());
            vo.setMentorIds(ip.getMentorIds());
            vo.setCompletionStatus(ip.getCompletionStatus());
            vo.setCompletionPackageUrl(ip.getCompletionPackageUrl());
            vo.setCompletionDesc(ip.getCompletionDesc());
            Project p = projectService.getById(ip.getProjectId());
            if (p != null) {
                vo.setProjectName(p.getProjectName());
                vo.setProjectStatus(p.getStatus());
            }
            return vo;
        }).collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 导师/专家：查看我辅导的孵化项目列表
     * 规则：
     * 1. 通过 mentor_ids 字段匹配（管理员手动分配的）
     * 2. 根据项目 tech_domain 与专家 field 自动匹配（申报环节选择的专业领域）
     */
    @GetMapping("/mentor/list")
    public Result<List<IncubationAdminVO>> mentorList(@RequestParam Integer mentorId) {
        java.util.Set<Integer> matchedProjectIds = new java.util.HashSet<>();
        List<IncubationAdminVO> vos = new ArrayList<>();
        
        // 1. 通过 mentor_ids 字段匹配
        QueryWrapper<IncubationProject> query = new QueryWrapper<>();
        query.apply("FIND_IN_SET({0}, mentor_ids)", mentorId);
        List<IncubationProject> assignedIncubations = incubationProjectService.list(query);
        for (IncubationProject ip : assignedIncubations) {
            IncubationAdminVO vo = buildIncubationAdminVO(ip);
            vos.add(vo);
            matchedProjectIds.add(ip.getProjectId());
        }
        
        // 2. 根据项目领域自动匹配专家
        com.example.concept_platform.entity.SysUser expert = sysUserService.getById(mentorId);
        if (expert != null && expert.getField() != null && expert.getRole() != null && 
            (expert.getRole().equals("EXPERT") || expert.getRole().equals("MENTOR"))) {
            String expertField = expert.getField().trim();
            List<IncubationProject> allIncubations = incubationProjectService.list();
            
            for (IncubationProject ip : allIncubations) {
                if (matchedProjectIds.contains(ip.getProjectId())) {
                    continue; // 已匹配过，跳过
                }
                Project p = projectService.getById(ip.getProjectId());
                if (p == null || p.getTechDomain() == null) continue;
                
                // 解析项目的 tech_domain
                boolean matched = false;
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<String> domains = mapper.readValue(
                            p.getTechDomain(), 
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
                    matched = domains.stream().anyMatch(domain -> 
                            domain.contains(expertField) || expertField.contains(domain) ||
                            normalizeDomainForMatching(domain).equals(normalizeDomainForMatching(expertField)));
                } catch (Exception e) {
                    // JSON解析失败，尝试字符串匹配
                    matched = p.getTechDomain().contains(expertField) || expertField.contains(p.getTechDomain());
                }
                
                if (matched) {
                    IncubationAdminVO vo = buildIncubationAdminVO(ip);
                    vos.add(vo);
                    matchedProjectIds.add(ip.getProjectId());
                }
            }
        }
        
        return Result.success(vos);
    }
    
    private IncubationAdminVO buildIncubationAdminVO(IncubationProject ip) {
        IncubationAdminVO vo = new IncubationAdminVO();
        vo.setIncubationId(ip.getIncubationId());
        vo.setProjectId(ip.getProjectId());
        vo.setStatus(ip.getStatus());
        vo.setProjectManagerId(ip.getProjectManagerId());
        vo.setMentorIds(ip.getMentorIds());
        vo.setCompletionStatus(ip.getCompletionStatus());
        vo.setCompletionPackageUrl(ip.getCompletionPackageUrl());
        vo.setCompletionDesc(ip.getCompletionDesc());
        Project p = projectService.getById(ip.getProjectId());
        if (p != null) {
            vo.setProjectName(p.getProjectName());
            vo.setProjectStatus(p.getStatus());
        }
        return vo;
    }
    
    private String normalizeDomainForMatching(String domain) {
        if (domain == null || domain.isBlank()) return "其他";
        String normalized = domain.trim();
        if (normalized.contains("人工智能") || normalized.contains("AI")) return "人工智能";
        if (normalized.contains("大数据") || normalized.contains("云计算")) return "大数据与云计算";
        if (normalized.contains("信息安全") || normalized.contains("密码")) return "信息安全";
        if (normalized.contains("物联网") || normalized.contains("嵌入式")) return "物联网与嵌入式技术";
        if (normalized.contains("数字人文") || normalized.contains("古籍")) return "数字人文技术";
        return "其他";
    }

    /**
     * 管理员：将已通过评审的项目标记为“准予入孵”，创建孵化项目记录
     */
    @PostMapping("/approve")
    public Result<Boolean> approveForIncubation(@RequestBody ApproveIncubationDto dto) {
        if (dto.getProjectId() == null) {
            return Result.error("项目ID不能为空");
        }
        Project project = projectService.getById(dto.getProjectId());
        if (project == null) {
            return Result.error("项目不存在");
        }

        // 将项目状态设置为 4-待入孵
        project.setStatus(4);
        projectService.updateById(project);

        // 如果尚未创建孵化记录，则创建
        QueryWrapper<IncubationProject> query = new QueryWrapper<>();
        query.eq("project_id", dto.getProjectId());
        IncubationProject existing = incubationProjectService.getOne(query);
        if (existing == null) {
            IncubationProject incubationProject = new IncubationProject();
            incubationProject.setProjectId(dto.getProjectId());
            incubationProject.setStatus(0); // 待签署协议
            incubationProject.setCompletionStatus(0); // 落成状态：未申请
            incubationProjectService.save(incubationProject);
        }
        return Result.success(true);
    }

    /**
     * 申报人：我的孵化项目列表
     * 规则：
     * - 先找到该申报人的所有项目；
     * - 对状态 >= 3（已入库及之后）且未驳回的项目，如无孵化记录则自动创建一条 status=0；
     * - 返回包含项目名称的 VO 列表。
     */
    @GetMapping("/my")
    public Result<List<ApplicantIncubationVO>> myIncubationProjects(@RequestParam Integer applicantId) {
        List<IncubationProject> incubations = getOrCreateIncubationsByApplicant(applicantId);
        List<ApplicantIncubationVO> vos = incubations.stream().map(ip -> {
            ApplicantIncubationVO vo = new ApplicantIncubationVO();
            vo.setIncubationId(ip.getIncubationId());
            vo.setProjectId(ip.getProjectId());
            vo.setStatus(ip.getStatus());
            vo.setCompletionStatus(ip.getCompletionStatus());
            vo.setCreatedAt(ip.getCreatedAt());
            Project p = projectService.getById(ip.getProjectId());
            if (p != null) {
                vo.setProjectName(p.getProjectName());
            }
            return vo;
        }).collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 申报人：提交入孵协议与信息表
     */
    @PostMapping("/submit-agreement")
    public Result<Boolean> submitAgreement(@RequestBody SubmitAgreementDto dto) {
        if (dto.getIncubationId() == null) {
            return Result.error("孵化项目ID不能为空");
        }
        IncubationProject incubationProject = incubationProjectService.getById(dto.getIncubationId());
        if (incubationProject == null) {
            return Result.error("孵化项目不存在");
        }
        incubationProject.setAgreementUrl(dto.getAgreementUrl());
        incubationProject.setInfoFormJson(dto.getInfoFormJson());
        incubationProject.setStatus(1); // 协议已提交，进入孵化中

        // 同步更新项目状态为 5-孵化中
        Project project = projectService.getById(incubationProject.getProjectId());
        if (project != null) {
            project.setStatus(5);
            projectService.updateById(project);
        }
        return Result.success(incubationProjectService.updateById(incubationProject));
    }

    /**
     * 管理员：为孵化项目指派项目经理与导师，并可同时创建若干里程碑
     */
    @PostMapping("/assign")
    public Result<Boolean> assignManagerAndMentors(@RequestBody AssignManagerDto dto) {
        if (dto.getIncubationId() == null) {
            return Result.error("孵化项目ID不能为空");
        }
        IncubationProject incubationProject = incubationProjectService.getById(dto.getIncubationId());
        if (incubationProject == null) {
            return Result.error("孵化项目不存在");
        }
        incubationProject.setProjectManagerId(dto.getProjectManagerId());
        incubationProject.setMentorIds(dto.getMentorIds());
        incubationProjectService.updateById(incubationProject);

        // 如果未指定里程碑，自动创建默认的两个里程碑：中期审核和最终定型
        QueryWrapper<IncubationMilestone> existingQuery = new QueryWrapper<>();
        existingQuery.eq("incubation_id", dto.getIncubationId());
        long existingCount = incubationMilestoneService.count(existingQuery);
        
        if (existingCount == 0) {
            // 自动创建中期审核里程碑
            IncubationMilestone midterm = new IncubationMilestone();
            midterm.setIncubationId(dto.getIncubationId());
            midterm.setName("中期审核");
            midterm.setDescription("提交中期进展报告，包括项目进展、遇到的问题、下一步计划等");
            midterm.setDeliverables("中期进展报告（包含项目进展、遇到的问题、下一步计划）、相关附件材料");
            midterm.setDeadline(LocalDateTime.now().plusMonths(3)); // 默认3个月后
            midterm.setStatus(1); // 进行中
            midterm.setCreatedAt(LocalDateTime.now());
            incubationMilestoneService.save(midterm);
            
            // 自动创建最终定型里程碑
            IncubationMilestone finalMilestone = new IncubationMilestone();
            finalMilestone.setIncubationId(dto.getIncubationId());
            finalMilestone.setName("最终定型");
            finalMilestone.setDescription("提交最终成果材料，完成孵化立项，包括最终报告、产品/服务描述、知识产权证书、客户意向书、融资状态等");
            finalMilestone.setDeliverables("最终报告、产品/服务描述、知识产权证书、客户意向书、融资状态等完整材料");
            finalMilestone.setDeadline(LocalDateTime.now().plusMonths(6)); // 默认6个月后
            finalMilestone.setStatus(0); // 未开始
            finalMilestone.setCreatedAt(LocalDateTime.now());
            incubationMilestoneService.save(finalMilestone);
        }

        // 创建自定义里程碑（如有）
        if (dto.getMilestones() != null && !dto.getMilestones().isEmpty()) {
            for (AssignManagerDto.MilestoneDef def : dto.getMilestones()) {
                IncubationMilestone milestone = new IncubationMilestone();
                milestone.setIncubationId(dto.getIncubationId());
                milestone.setName(def.getName());
                milestone.setDescription(def.getDescription());
                milestone.setDeliverables(def.getDeliverables());
                milestone.setDeadline(def.getDeadline());
                milestone.setStatus(1); // 进行中
                milestone.setCreatedAt(LocalDateTime.now());
                incubationMilestoneService.save(milestone);
            }
        }
        return Result.success(true);
    }

    /**
     * 孵化项目：获取某项目下的里程碑列表
     */
    @GetMapping("/{incubationId}/milestones")
    public Result<List<IncubationMilestone>> listMilestones(@PathVariable Integer incubationId) {
        QueryWrapper<IncubationMilestone> query = new QueryWrapper<>();
        query.eq("incubation_id", incubationId);
        query.orderByAsc("deadline");
        return Result.success(incubationMilestoneService.list(query));
    }

    /**
     * 管理员：通知申报人提交里程碑报告
     */
    @PostMapping("/milestone/admin-notify")
    public Result<Boolean> adminNotifyMilestone(@RequestBody AdminNotifyMilestoneDto dto) {
        if (dto.getMilestoneId() == null) {
            return Result.error("里程碑ID不能为空");
        }
        IncubationMilestone milestone = incubationMilestoneService.getById(dto.getMilestoneId());
        if (milestone == null) {
            return Result.error("里程碑不存在");
        }
        // 检查是否已有报告
        QueryWrapper<IncubationMilestoneReport> reportQuery = new QueryWrapper<>();
        reportQuery.eq("milestone_id", dto.getMilestoneId());
        IncubationMilestoneReport report = milestoneReportService.getOne(reportQuery);
        if (report == null) {
            // 创建新报告记录，标记为已通知
            report = new IncubationMilestoneReport();
            report.setMilestoneId(dto.getMilestoneId());
            IncubationProject ip = incubationProjectService.getById(milestone.getIncubationId());
            if (ip != null) {
                report.setProjectId(ip.getProjectId());
            }
            report.setStatus(0); // 待提交
            report.setAdminApproved(0); // 待管理员审批
            report.setAdminNotified(1); // 已通知
            report.setAdminNotifyTime(LocalDateTime.now());
            report.setCreatedAt(LocalDateTime.now());
            milestoneReportService.save(report);
        } else {
            // 更新通知状态
            report.setAdminNotified(1);
            report.setAdminNotifyTime(LocalDateTime.now());
            milestoneReportService.updateById(report);
        }
        // 里程碑状态置为进行中（等待申报人提交）
        milestone.setStatus(1);
        incubationMilestoneService.updateById(milestone);
        return Result.success(true);
    }

    /**
     * 申报人：提交里程碑进展报告（包含项目描述、进展内容、附件等）
     */
    @PostMapping("/milestone/submit-report")
    public Result<Boolean> submitMilestoneReport(@RequestBody SubmitMilestoneReportDto dto) {
        if (dto.getMilestoneId() == null) {
            return Result.error("里程碑ID不能为空");
        }
        IncubationMilestone milestone = incubationMilestoneService.getById(dto.getMilestoneId());
        if (milestone == null) {
            return Result.error("里程碑不存在");
        }
        // 不再强制要求“已收到管理员通知”才能提交：
        // 如果不存在报告记录，则自动创建一条；如果已存在，则在原记录基础上更新内容。
        QueryWrapper<IncubationMilestoneReport> reportQuery = new QueryWrapper<>();
        reportQuery.eq("milestone_id", dto.getMilestoneId());
        reportQuery.orderByDesc("created_at");
        reportQuery.last("LIMIT 1");
        IncubationMilestoneReport report = milestoneReportService.getOne(reportQuery);
        if (report == null) {
            // 首次提交，自动创建报告记录
            report = new IncubationMilestoneReport();
            report.setMilestoneId(dto.getMilestoneId());
            // 通过孵化项目补全 projectId
            IncubationProject ip = incubationProjectService.getById(milestone.getIncubationId());
            if (ip != null) {
                report.setProjectId(ip.getProjectId());
            }
            report.setCreatedAt(LocalDateTime.now());
        }
        // 更新/覆盖报告内容
        report.setProjectDescription(dto.getProjectDescription()); // 项目描述
        report.setContent(dto.getContent()); // 进展内容
        report.setAttachments(dto.getAttachments()); // 附件
        report.setSubmitterId(dto.getSubmitterId());
        report.setStatus(0); // 待审核
        report.setAdminApproved(0); // 待管理员审批
        report.setExpertScore(null); // 清空专家评分
        report.setExpertFeedback(null);
        report.setAdminFeedback(null);
        // 根据 reportId 判定是新建还是更新
        if (report.getReportId() == null) {
            milestoneReportService.save(report);
        } else {
            milestoneReportService.updateById(report);
        }

        // 里程碑状态置为待审核
        milestone.setStatus(2);
        incubationMilestoneService.updateById(milestone);
        return Result.success(true);
    }

    /**
     * 管理员：审批里程碑报告（申报人提交后，管理员先初审）
     * 如果通过，推送给专家；如果拒绝，需要申报人修改
     */
    @PostMapping("/milestone/admin-approve")
    public Result<Boolean> adminApproveMilestoneReport(@RequestBody AdminApproveReportDto dto) {
        if (dto.getMilestoneId() == null || dto.getApproved() == null) {
            return Result.error("里程碑ID与审批结果不能为空");
        }
        QueryWrapper<IncubationMilestoneReport> query = new QueryWrapper<>();
        query.eq("milestone_id", dto.getMilestoneId());
        query.orderByDesc("created_at");
        query.last("LIMIT 1");
        IncubationMilestoneReport report = milestoneReportService.getOne(query);
        if (report == null) {
            return Result.error("报告不存在");
        }
        if (report.getStatus() == null || report.getStatus() != 0) {
            return Result.error("报告状态不正确，无法审批");
        }
        report.setAdminApproved(dto.getApproved() ? 1 : 2); // 1-通过（推送给专家）, 2-拒绝（需修改）
        report.setAdminFeedback(dto.getFeedback());
        if (dto.getApproved()) {
            // 管理员审批通过，推送给专家，状态仍为待审核（等待专家打分）
            report.setStatus(0); // 保持待审核状态，等待专家打分
        } else {
            // 管理员审批拒绝，需要申报人修改
            report.setStatus(2); // 需修订
        }
        milestoneReportService.updateById(report);
        
        // 同步更新里程碑状态
        IncubationMilestone milestone = incubationMilestoneService.getById(dto.getMilestoneId());
        if (milestone != null) {
            if (dto.getApproved()) {
                milestone.setStatus(2); // 待专家审核
            } else {
                milestone.setStatus(4); // 需修订
            }
            incubationMilestoneService.updateById(milestone);
        }
        return Result.success(true);
    }

    /**
     * 专家：为里程碑报告打分（管理员审批通过后，专家才能打分）
     */
    @PostMapping("/milestone/expert-score")
    public Result<Boolean> expertScoreMilestoneReport(@RequestBody ExpertScoreReportDto dto) {
        if (dto.getMilestoneId() == null || dto.getScore() == null) {
            return Result.error("里程碑ID与评分不能为空");
        }
        QueryWrapper<IncubationMilestoneReport> query = new QueryWrapper<>();
        query.eq("milestone_id", dto.getMilestoneId());
        query.orderByDesc("created_at");
        query.last("LIMIT 1");
        IncubationMilestoneReport report = milestoneReportService.getOne(query);
        if (report == null) {
            return Result.error("报告不存在");
        }
        // 检查是否已通过管理员审批
        if (report.getAdminApproved() == null || report.getAdminApproved() != 1) {
            return Result.error("该报告尚未通过管理员审批，无法打分");
        }
        report.setExpertScore(dto.getScore());
        report.setExpertFeedback(dto.getFeedback());
        // 专家打分后，报告状态更新为已通过
        report.setStatus(1);
        milestoneReportService.updateById(report);

        // 同步更新里程碑状态为已通过
        IncubationMilestone milestone = incubationMilestoneService.getById(report.getMilestoneId());
        if (milestone != null) {
            milestone.setStatus(3); // 已通过
            incubationMilestoneService.updateById(milestone);
        }
        return Result.success(true);
    }

    /**
     * 项目经理/导师：审核里程碑报告（保留原有接口，用于非审批流程的反馈）
     */
    @PostMapping("/milestone/review-report")
    public Result<Boolean> reviewMilestoneReport(@RequestBody ReviewMilestoneReportDto dto) {
        if (dto.getMilestoneId() == null || dto.getPass() == null) {
            return Result.error("里程碑ID与审核结果不能为空");
        }
        QueryWrapper<IncubationMilestoneReport> query = new QueryWrapper<>();
        query.eq("milestone_id", dto.getMilestoneId());
        query.orderByDesc("created_at");
        query.last("LIMIT 1");
        IncubationMilestoneReport report = milestoneReportService.getOne(query);
        if (report == null) {
            return Result.error("报告不存在");
        }
        report.setMentorFeedback(dto.getFeedback());
        // 如果管理员已审批通过，则不能通过此接口修改状态
        if (report.getAdminApproved() != null && report.getAdminApproved() == 1) {
            return Result.error("该报告已通过管理员审批，请使用专家打分接口");
        }
        report.setStatus(dto.getPass() ? 1 : 2);
        milestoneReportService.updateById(report);

        // 同步更新里程碑状态
        IncubationMilestone milestone = incubationMilestoneService.getById(report.getMilestoneId());
        if (milestone != null) {
            milestone.setStatus(dto.getPass() ? 3 : 4);
            incubationMilestoneService.updateById(milestone);
        }
        return Result.success(true);
    }

    /**
     * 查询某里程碑下的报告列表（目前一个里程碑只保留一条最新报告）
     */
    @GetMapping("/milestone/{milestoneId}/reports")
    public Result<List<IncubationMilestoneReport>> listReportsByMilestone(@PathVariable Integer milestoneId) {
        QueryWrapper<IncubationMilestoneReport> query = new QueryWrapper<>();
        query.eq("milestone_id", milestoneId);
        query.orderByDesc("created_at");
        return Result.success(milestoneReportService.list(query));
    }

    /**
     * 申报人：提交资源申请
     */
    @PostMapping("/resource/apply")
    public Result<Boolean> applyResource(@RequestBody ResourceApplyDto dto) {
        if (dto.getIncubationId() == null) {
            return Result.error("孵化项目ID不能为空");
        }
        IncubationResourceRequest request = new IncubationResourceRequest();
        request.setIncubationId(dto.getIncubationId());
        request.setProjectId(dto.getProjectId());
        request.setRequesterId(dto.getRequesterId());
        request.setType(dto.getType());
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setStatus(0);
        request.setCreatedAt(LocalDateTime.now());
        return Result.success(resourceRequestService.save(request));
    }

    /**
     * 项目经理/专家：处理资源申请
     */
    @PostMapping("/resource/handle")
    public Result<Boolean> handleResource(@RequestBody ResourceHandleDto dto) {
        if (dto.getRequestId() == null) {
            return Result.error("工单ID必填");
        }
        IncubationResourceRequest request = resourceRequestService.getById(dto.getRequestId());
        if (request == null) {
            return Result.error("资源申请不存在");
        }
        // 专家/项目经理处理后，直接视为完成分配，状态从待处理(0)直接变为已完成(2)
        request.setStatus(2);
        if (dto.getHandlerId() != null) {
            request.setHandlerId(dto.getHandlerId());
        }
        if (dto.getHistoryJson() != null && !dto.getHistoryJson().isEmpty()) {
            // 追加处理历史
            String existingHistory = request.getHistoryJson();
            String newHistory = dto.getHistoryJson();
            if (existingHistory != null && !existingHistory.isEmpty()) {
                // 如果已有历史，追加新记录
                request.setHistoryJson(existingHistory + "\n" + newHistory);
            } else {
                request.setHistoryJson(newHistory);
            }
        }
        request.setUpdatedAt(LocalDateTime.now());
        boolean result = resourceRequestService.updateById(request);
        if (!result) {
            return Result.error("更新资源申请失败");
        }
        return Result.success(true);
    }

    /**
     * 申报人：查看我提交的资源申请及处理结果（用于弹窗提醒）
     */
    @GetMapping("/resource/applicant/list")
    public Result<List<ResourceRequestVO>> applicantResourceList(@RequestParam Integer applicantId) {
        if (applicantId == null) {
            return Result.error("申请人ID不能为空");
        }
        QueryWrapper<IncubationResourceRequest> query = new QueryWrapper<>();
        query.eq("requester_id", applicantId);
        query.orderByDesc("created_at");
        List<IncubationResourceRequest> requests = resourceRequestService.list(query);
        List<ResourceRequestVO> vos = requests.stream().map(req -> {
            ResourceRequestVO vo = new ResourceRequestVO();
            vo.setRequestId(req.getRequestId());
            vo.setIncubationId(req.getIncubationId());
            vo.setProjectId(req.getProjectId());
            Project p = projectService.getById(req.getProjectId());
            vo.setProjectName(p != null ? p.getProjectName() : "项目ID:" + req.getProjectId());
            vo.setRequesterId(req.getRequesterId());
            vo.setType(req.getType());
            vo.setTitle(req.getTitle());
            vo.setDescription(req.getDescription());
            vo.setStatus(req.getStatus());
            vo.setHandlerId(req.getHandlerId());
            vo.setHistoryJson(req.getHistoryJson());
            vo.setCreatedAt(req.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 专家：查看我负责的孵化项目的资源申请列表
     */
    @GetMapping("/resource/mentor/list")
    public Result<List<ResourceRequestVO>> mentorResourceList(@RequestParam Integer mentorId) {
        // 先获取该专家负责的所有孵化项目
        List<IncubationAdminVO> myProjects = mentorList(mentorId).getData();
        if (myProjects == null || myProjects.isEmpty()) {
            return Result.success(List.of());
        }
        List<Integer> incubationIds = myProjects.stream()
                .map(IncubationAdminVO::getIncubationId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        
        if (incubationIds.isEmpty()) {
            return Result.success(List.of());
        }
        
        // 查询这些项目的资源申请
        QueryWrapper<IncubationResourceRequest> query = new QueryWrapper<>();
        query.in("incubation_id", incubationIds);
        query.orderByDesc("created_at");
        List<IncubationResourceRequest> requests = resourceRequestService.list(query);
        
        if (requests == null || requests.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<ResourceRequestVO> vos = requests.stream().map(req -> {
            ResourceRequestVO vo = new ResourceRequestVO();
            vo.setRequestId(req.getRequestId());
            vo.setIncubationId(req.getIncubationId());
            vo.setProjectId(req.getProjectId());
            Project p = null;
            if (req.getProjectId() != null) {
                p = projectService.getById(req.getProjectId());
            }
            vo.setProjectName(p != null ? p.getProjectName() : "项目ID:" + (req.getProjectId() != null ? req.getProjectId() : "未知"));
            vo.setRequesterId(req.getRequesterId());
            vo.setType(req.getType());
            vo.setTitle(req.getTitle());
            vo.setDescription(req.getDescription());
            vo.setStatus(req.getStatus());
            vo.setHandlerId(req.getHandlerId());
            vo.setHistoryJson(req.getHistoryJson());
            vo.setCreatedAt(req.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(vos);
    }

    /**
     * 申报人：提交落成申请（完成至少一轮里程碑审核后）
     */
    @PostMapping("/completion/apply")
    public Result<Boolean> applyCompletion(@RequestBody CompletionApplyDto dto) {
        if (dto.getIncubationId() == null) {
            return Result.error("孵化项目ID不能为空");
        }
        IncubationProject incubationProject = incubationProjectService.getById(dto.getIncubationId());
        if (incubationProject == null) {
            return Result.error("孵化项目不存在");
        }
        // 检查是否已完成至少一轮里程碑审核（中期汇报通过即可发起落成申请）
        QueryWrapper<IncubationMilestone> milestoneQuery = new QueryWrapper<>();
        milestoneQuery.eq("incubation_id", dto.getIncubationId());
        List<IncubationMilestone> milestones = incubationMilestoneService.list(milestoneQuery);
        long completedCount = milestones.stream()
                .filter(m -> m.getStatus() != null && m.getStatus() == 3) // 已通过
                .count();
        if (completedCount < 1) {
            return Result.error("需要至少完成一轮里程碑审核（中期汇报通过）后才能申请落成");
        }
        // 1-待平台初审（管理员）
        incubationProject.setCompletionStatus(1);
        incubationProject.setCompletionPackageUrl(dto.getCompletionPackageUrl());
        incubationProject.setCompletionDesc(dto.getCompletionDesc());
        return Result.success(incubationProjectService.updateById(incubationProject));
    }

    /**
     * 管理员：落成初审结果
     */
    @PostMapping("/completion/admin-review")
    public Result<Boolean> adminReviewCompletion(@RequestBody CompletionReviewDto dto) {
        if (dto.getIncubationId() == null || dto.getPass() == null) {
            return Result.error("孵化项目ID与审核结果不能为空");
        }
        IncubationProject incubationProject = incubationProjectService.getById(dto.getIncubationId());
        if (incubationProject == null) {
            return Result.error("孵化项目不存在");
        }
        // 通过：2-待专家终审；拒绝：4-平台初审拒绝
        incubationProject.setCompletionStatus(dto.getPass() ? 2 : 4);
        incubationProjectService.updateById(incubationProject);
        return Result.success(true);
    }

    /**
     * 兼容旧接口：直接等价于管理员初审
     */
    @PostMapping("/completion/review")
    public Result<Boolean> reviewCompletion(@RequestBody CompletionReviewDto dto) {
        return adminReviewCompletion(dto);
    }

    /**
     * 专家：落成终审打分
     * 规则：
     * - score >= 60：视为通过，项目标记为已落成，自动进入成功案例库；
     * - score < 60：视为终审拒绝，仅记录评分与反馈，不做落成与成功案例处理。
     */
    @PostMapping("/completion/expert-review")
    public Result<Boolean> expertReviewCompletion(@RequestBody ExpertCompletionReviewDto dto) {
        if (dto.getIncubationId() == null || dto.getScore() == null) {
            return Result.error("孵化项目ID与评分不能为空");
        }
        IncubationProject incubationProject = incubationProjectService.getById(dto.getIncubationId());
        if (incubationProject == null) {
            return Result.error("孵化项目不存在");
        }
        Project project = projectService.getById(incubationProject.getProjectId());
        if (project == null) {
            return Result.error("主项目信息不存在");
        }

        boolean pass = dto.getScore() >= 60;

        if (pass) {
            // 3-已落成
            incubationProject.setCompletionStatus(3);
            incubationProject.setStatus(2); // 孵化项目状态：已落成
            incubationProjectService.updateById(incubationProject);

            // 主项目状态置为已落成
            project.setStatus(6);
            projectService.updateById(project);

            // 自动创建成功案例记录（如果不存在），用于成功案例库和统计图展示
            QueryWrapper<SuccessCase> caseQuery = new QueryWrapper<>();
            caseQuery.eq("incubation_id", incubationProject.getIncubationId());
            SuccessCase existing = successCaseService.getOne(caseQuery);
            if (existing == null) {
                SuccessCase successCase = new SuccessCase();
                successCase.setProjectId(project.getProjectId());
                successCase.setIncubationId(incubationProject.getIncubationId());
                successCase.setCaseName(project.getProjectName());
                successCase.setCaseDescription(project.getDescription());
                successCase.setTechDomain(project.getTechDomain());
                successCase.setApplicationScenario(project.getApplicationScenario());
                successCase.setIntellectualProperty(project.getIntellectualProperty());
                successCase.setCooperationMode(project.getCooperationNeed());
                successCase.setContactInfo(null);
                successCase.setDisplayStatus(1);
                successCase.setViewCount(0);
                successCaseService.save(successCase);
            }
        } else {
            // 5-专家终审拒绝，仅更新落成状态
            incubationProject.setCompletionStatus(5);
            incubationProjectService.updateById(incubationProject);
        }

        return Result.success(true);
    }

    /**
     * 专家：查看待终审的落成申请列表（completionStatus = 2）
     */
    @GetMapping("/completion/expert/pending")
    public Result<List<CompletionReviewVO>> getPendingCompletionReviews(@RequestParam Integer mentorId) {
        // 复用 mentorList 逻辑获取该专家相关的孵化项目
        List<IncubationAdminVO> myProjects = mentorList(mentorId).getData();
        if (myProjects == null || myProjects.isEmpty()) {
            return Result.success(List.of());
        }

        List<CompletionReviewVO> vos = new ArrayList<>();
        for (IncubationAdminVO vo : myProjects) {
            if (vo.getIncubationId() == null) {
                continue;
            }
            IncubationProject ip = incubationProjectService.getById(vo.getIncubationId());
            if (ip == null) {
                continue;
            }
            if (ip.getCompletionStatus() == null || ip.getCompletionStatus() != 2) {
                // 只返回已通过平台初审、待专家终审的项目
                continue;
            }
            Project p = projectService.getById(ip.getProjectId());
            CompletionReviewVO item = new CompletionReviewVO();
            item.setIncubationId(ip.getIncubationId());
            item.setProjectId(ip.getProjectId());
            item.setProjectName(p != null ? p.getProjectName() : vo.getProjectName());
            item.setCompletionStatus(ip.getCompletionStatus());
            item.setCompletionPackageUrl(ip.getCompletionPackageUrl());
            item.setCompletionDesc(ip.getCompletionDesc());
            vos.add(item);
        }
        return Result.success(vos);
    }

    /**
     * 申报人：待提交的提醒（包含待签署协议、中期汇报、最终提交三类）
     */
    @GetMapping("/reminders")
    public Result<ReminderVO> reminders(@RequestParam Integer applicantId) {
        List<IncubationProject> incubations = getOrCreateIncubationsByApplicant(applicantId);
        
        ReminderVO vo = new ReminderVO();
        
        // 1. 待签署协议（status=0）
        List<ReminderItem> agreementReminders = incubations.stream()
                .filter(ip -> ip.getStatus() != null && ip.getStatus() == 0)
                .map(ip -> {
                    Project p = projectService.getById(ip.getProjectId());
                    ReminderItem item = new ReminderItem();
                    item.setIncubationId(ip.getIncubationId());
                    item.setProjectId(ip.getProjectId());
                    item.setProjectName(p != null ? p.getProjectName() : "项目ID:" + ip.getProjectId());
                    item.setType("协议签署");
                    item.setMessage("请尽快在线签署《孵化服务协议》并填写《入孵信息表》");
                    item.setCreatedAt(ip.getCreatedAt());
                    return item;
                })
                .collect(Collectors.toList());
        vo.setAgreementReminders(agreementReminders);
        
        // 2. 中期汇报提醒（孵化中项目，有"中期审核"里程碑且未提交报告或需修订）
        List<ReminderItem> midtermReminders = new ArrayList<>();
        for (IncubationProject ip : incubations) {
            if (ip.getStatus() != null && ip.getStatus() == 1) { // 孵化中
                QueryWrapper<IncubationMilestone> milestoneQuery = new QueryWrapper<>();
                milestoneQuery.eq("incubation_id", ip.getIncubationId());
                milestoneQuery.and(wrapper -> wrapper.like("name", "中期").or().like("name", "中期审核"));
                milestoneQuery.and(wrapper -> wrapper.eq("status", 1).or().eq("status", 4)); // 进行中或需修订
                List<IncubationMilestone> milestones = incubationMilestoneService.list(milestoneQuery);
                
                for (IncubationMilestone m : milestones) {
                    // 检查是否已提交报告
                    QueryWrapper<IncubationMilestoneReport> reportQuery = new QueryWrapper<>();
                    reportQuery.eq("milestone_id", m.getMilestoneId());
                    reportQuery.eq("status", 1); // 已通过的报告
                    long passedCount = milestoneReportService.count(reportQuery);
                    
                    if (passedCount == 0) { // 未通过或未提交
                        Project p = projectService.getById(ip.getProjectId());
                        ReminderItem item = new ReminderItem();
                        item.setIncubationId(ip.getIncubationId());
                        item.setProjectId(ip.getProjectId());
                    item.setProjectName(p != null ? p.getProjectName() : "项目ID:" + ip.getProjectId());
                    item.setType("中期汇报");
                    item.setMessage("请提交中期进展报告材料，包括：项目进展说明、遇到的问题、下一步计划、相关附件等。截止日期：" + 
                            (m.getDeadline() != null ? m.getDeadline().toString().substring(0, 10) : "待定"));
                    item.setMilestoneId(m.getMilestoneId());
                    item.setMilestoneName(m.getName());
                    item.setDeadline(m.getDeadline());
                    midtermReminders.add(item);
                    }
                }
            }
        }
        vo.setMidtermReminders(midtermReminders);
        
        // 3. 最终提交提醒（孵化中项目，有"最终定型"里程碑且未提交报告）
        List<ReminderItem> finalReminders = new ArrayList<>();
        for (IncubationProject ip : incubations) {
            if (ip.getStatus() != null && ip.getStatus() == 1) { // 孵化中
                QueryWrapper<IncubationMilestone> milestoneQuery = new QueryWrapper<>();
                milestoneQuery.eq("incubation_id", ip.getIncubationId());
                milestoneQuery.and(wrapper -> wrapper.like("name", "最终").or().like("name", "最终定型"));
                milestoneQuery.and(wrapper -> wrapper.eq("status", 0).or().eq("status", 1).or().eq("status", 4)); // 未开始、进行中或需修订
                List<IncubationMilestone> milestones = incubationMilestoneService.list(milestoneQuery);
                
                for (IncubationMilestone m : milestones) {
                    // 检查是否已提交并通过报告
                    QueryWrapper<IncubationMilestoneReport> reportQuery = new QueryWrapper<>();
                    reportQuery.eq("milestone_id", m.getMilestoneId());
                    reportQuery.eq("status", 1); // 已通过的报告
                    long passedCount = milestoneReportService.count(reportQuery);
                    
                    if (passedCount == 0) { // 未通过或未提交
                        Project p = projectService.getById(ip.getProjectId());
                        ReminderItem item = new ReminderItem();
                        item.setIncubationId(ip.getIncubationId());
                        item.setProjectId(ip.getProjectId());
                    item.setProjectName(p != null ? p.getProjectName() : "项目ID:" + ip.getProjectId());
                    item.setType("最终提交");
                    item.setMessage("请提交最终成果材料，完成孵化立项，包括：最终报告、产品/服务描述、知识产权证书、客户意向书、融资状态等完整材料。截止日期：" + 
                            (m.getDeadline() != null ? m.getDeadline().toString().substring(0, 10) : "待定"));
                    item.setMilestoneId(m.getMilestoneId());
                    item.setMilestoneName(m.getName());
                    item.setDeadline(m.getDeadline());
                    finalReminders.add(item);
                    }
                }
            }
        }
        vo.setFinalReminders(finalReminders);
        
        return Result.success(vo);
    }

    // === DTO 定义 ===

    @Data
    public static class ApproveIncubationDto {
        private Integer projectId;
    }

    @Data
    public static class SubmitAgreementDto {
        private Integer incubationId;
        private String agreementUrl;
        private String infoFormJson;
    }

    @Data
    public static class AssignManagerDto {
        private Integer incubationId;
        private Integer projectManagerId;
        /**
         * 逗号分隔的导师ID字符串，例如 "3,4,5"
         */
        private String mentorIds;
        private List<MilestoneDef> milestones;

        @Data
        public static class MilestoneDef {
            private String name;
            private String description;
            private String deliverables;
            private LocalDateTime deadline;
        }
    }

    @Data
    public static class AdminNotifyMilestoneDto {
        private Integer milestoneId;
    }

    @Data
    public static class SubmitMilestoneReportDto {
        private Integer milestoneId;
        private Integer projectId;
        private Integer submitterId;
        private String projectDescription; // 项目描述
        private String content; // 进展内容
        private String attachments; // 附件
    }

    @Data
    public static class ReviewMilestoneReportDto {
        private Integer milestoneId;
        private Boolean pass;
        private String feedback;
    }

    @Data
    public static class AdminApproveReportDto {
        private Integer milestoneId;
        private Boolean approved; // true-通过, false-拒绝
        private String feedback; // 管理员审批意见
    }

    @Data
    public static class ExpertScoreReportDto {
        private Integer milestoneId;
        private Integer score; // 0-100
        private String feedback;
    }

    @Data
    public static class IncubationAdminVO {
        private Integer incubationId;
        private Integer projectId;
        private String projectName;
        private Integer projectStatus;
        private Integer status;
        private Integer projectManagerId;
        private String mentorIds;
        // 落成相关字段，便于在管理员/专家视图中展示与操作
        private Integer completionStatus;
        private String completionPackageUrl;
        private String completionDesc;
    }

    @Data
    public static class ApplicantIncubationVO {
        private Integer incubationId;
        private Integer projectId;
        private String projectName;
        private Integer status;
        private Integer completionStatus; // 落成状态: 0-未申请, 1-已申请, 2-已落成, 3-已拒绝
        private LocalDateTime createdAt;
    }

    /**
     * 工具方法：按申请人获取/创建孵化项目列表
     */
    private List<IncubationProject> getOrCreateIncubationsByApplicant(Integer applicantId) {
        QueryWrapper<Project> projectQuery = new QueryWrapper<>();
        projectQuery.eq("applicant_id", applicantId);
        List<Project> projects = projectService.list(projectQuery);
        if (projects.isEmpty()) {
            return List.of();
        }
        // 对已入库及之后、且未驳回的项目，确保存在孵化记录
        for (Project p : projects) {
            Integer status = p.getStatus();
            if (status != null && status >= 3 && status != 9) {
                QueryWrapper<IncubationProject> query = new QueryWrapper<>();
                query.eq("project_id", p.getProjectId());
                IncubationProject existing = incubationProjectService.getOne(query);
                if (existing == null) {
                    IncubationProject incubation = new IncubationProject();
                    incubation.setProjectId(p.getProjectId());
                    incubation.setStatus(0); // 待签署协议
                    incubation.setCompletionStatus(0); // 落成状态：未申请
                    incubationProjectService.save(incubation);
                }
            }
        }
        List<Integer> projectIds = projects.stream().map(Project::getProjectId).toList();
        QueryWrapper<IncubationProject> queryAll = new QueryWrapper<>();
        queryAll.in("project_id", projectIds);
        return incubationProjectService.list(queryAll);
    }

    @Data
    public static class ResourceApplyDto {
        private Integer incubationId;
        private Integer projectId;
        private Integer requesterId;
        private String type;
        private String title;
        private String description;
    }

    @Data
    public static class ResourceHandleDto {
        private Integer requestId;
        private Integer status;
        private Integer handlerId;
        private String historyJson;
    }

    @Data
    public static class CompletionApplyDto {
        private Integer incubationId;
        private String completionPackageUrl;
        private String completionDesc;
    }

    @Data
    public static class CompletionReviewDto {
        private Integer incubationId;
        private Boolean pass;
    }

    @Data
    public static class ExpertCompletionReviewDto {
        private Integer incubationId;
        private Integer score; // 0-100分
        private String feedback; // 专家反馈意见
    }

    @Data
    public static class CompletionReviewVO {
        private Integer incubationId;
        private Integer projectId;
        private String projectName;
        private Integer completionStatus;
        private String completionPackageUrl;
        private String completionDesc;
    }

    @Data
    public static class ReminderVO {
        private List<ReminderItem> agreementReminders; // 待签署协议
        private List<ReminderItem> midtermReminders; // 中期汇报
        private List<ReminderItem> finalReminders; // 最终提交
    }

    @Data
    public static class ReminderItem {
        private Integer incubationId;
        private Integer projectId;
        private String projectName;
        private String type; // "协议签署" / "中期汇报" / "最终提交"
        private String message;
        private Integer milestoneId;
        private String milestoneName;
        private LocalDateTime deadline;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ResourceRequestVO {
        private Integer requestId;
        private Integer incubationId;
        private Integer projectId;
        private String projectName;
        private Integer requesterId;
        private String type;
        private String title;
        private String description;
        private Integer status;
        private Integer handlerId;
        private String historyJson;
        private LocalDateTime createdAt;
    }
}


