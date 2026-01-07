package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.IncubationResourceRequest;
import com.example.concept_platform.mapper.IncubationResourceRequestMapper;
import com.example.concept_platform.service.IIncubationResourceRequestService;
import org.springframework.stereotype.Service;

@Service
public class IncubationResourceRequestServiceImpl extends ServiceImpl<IncubationResourceRequestMapper, IncubationResourceRequest>
        implements IIncubationResourceRequestService {
}


