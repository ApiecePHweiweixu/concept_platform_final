package com.example.concept_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.concept_platform.entity.ResourceInventory;

import java.util.List;

public interface IResourceInventoryService extends IService<ResourceInventory> {
    List<ResourceInventory> listByType(String type);
}

