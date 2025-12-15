package com.example.concept_platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.concept_platform.entity.SysUser;
import com.example.concept_platform.mapper.SysUserMapper;
import com.example.concept_platform.service.ISysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    public SysUser login(String username, String password) {
        // Simple query by username
        SysUser user = this.getOne(new QueryWrapper<SysUser>().eq("username", username));
        
        // Check password (plain text as per requirement)
        if (user != null && user.getPassword().equals(password)) {
            // Hide password in return
            user.setPassword(null);
            return user;
        }
        return null;
    }
}

