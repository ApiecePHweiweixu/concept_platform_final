package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.IncubationProject;
import com.example.concept_platform.mapper.IncubationProjectMapper;
import com.example.concept_platform.service.IIncubationProjectService;
import org.springframework.stereotype.Service;

@Service
public class IncubationProjectServiceImpl extends ServiceImpl<IncubationProjectMapper, IncubationProject>
        implements IIncubationProjectService {
}


