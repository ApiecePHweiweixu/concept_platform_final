package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.Project;
import com.example.concept_platform.mapper.ProjectMapper;
import com.example.concept_platform.service.IProjectService;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {
}

