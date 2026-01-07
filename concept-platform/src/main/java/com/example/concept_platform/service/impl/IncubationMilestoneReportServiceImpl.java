package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.IncubationMilestoneReport;
import com.example.concept_platform.mapper.IncubationMilestoneReportMapper;
import com.example.concept_platform.service.IIncubationMilestoneReportService;
import org.springframework.stereotype.Service;

@Service
public class IncubationMilestoneReportServiceImpl extends ServiceImpl<IncubationMilestoneReportMapper, IncubationMilestoneReport>
        implements IIncubationMilestoneReportService {
}


