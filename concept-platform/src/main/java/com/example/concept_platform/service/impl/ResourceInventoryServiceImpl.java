package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.ResourceInventory;
import com.example.concept_platform.mapper.ResourceInventoryMapper;
import com.example.concept_platform.service.IResourceInventoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceInventoryServiceImpl extends ServiceImpl<ResourceInventoryMapper, ResourceInventory> implements IResourceInventoryService {
    @Override
    public List<ResourceInventory> listByType(String type) {
        QueryWrapper<ResourceInventory> query = new QueryWrapper<>();
        query.eq("resource_type", type);
        query.eq("status", 1);
        query.gt("remaining_quota", 0);
        return this.list(query);
    }
}

