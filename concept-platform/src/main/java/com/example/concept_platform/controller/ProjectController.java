package com.example.concept_platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.concept_platform.common.Result;
import com.example.concept_platform.entity.Project;
import com.example.concept_platform.service.IProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

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
    
    // Example of filtering by applicant (optional but useful)
    @GetMapping("/my-projects")
    public Result<List<Project>> myProjects(@RequestParam Integer applicantId) {
        QueryWrapper<Project> query = new QueryWrapper<>();
        query.eq("applicant_id", applicantId);
        return Result.success(projectService.list(query));
    }
}

