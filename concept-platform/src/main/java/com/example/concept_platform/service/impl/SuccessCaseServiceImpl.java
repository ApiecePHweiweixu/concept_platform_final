package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.SuccessCase;
import com.example.concept_platform.mapper.SuccessCaseMapper;
import com.example.concept_platform.service.ISuccessCaseService;
import org.springframework.stereotype.Service;

@Service
public class SuccessCaseServiceImpl extends ServiceImpl<SuccessCaseMapper, SuccessCase> implements ISuccessCaseService {
}

