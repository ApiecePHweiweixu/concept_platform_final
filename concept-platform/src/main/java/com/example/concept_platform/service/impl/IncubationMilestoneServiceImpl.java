package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.IncubationMilestone;
import com.example.concept_platform.mapper.IncubationMilestoneMapper;
import com.example.concept_platform.service.IIncubationMilestoneService;
import org.springframework.stereotype.Service;

@Service
public class IncubationMilestoneServiceImpl extends ServiceImpl<IncubationMilestoneMapper, IncubationMilestone>
        implements IIncubationMilestoneService {
}


