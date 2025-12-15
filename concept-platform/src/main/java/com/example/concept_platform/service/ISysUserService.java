package com.example.concept_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.concept_platform.entity.SysUser;

public interface ISysUserService extends IService<SysUser> {
    /**
     * User Login
     * @param username Account
     * @param password Password
     * @return User info or null if failed
     */
    SysUser login(String username, String password);
}

