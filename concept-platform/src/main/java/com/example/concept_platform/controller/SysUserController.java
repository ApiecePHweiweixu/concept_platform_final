package com.example.concept_platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.concept_platform.common.Result;
import com.example.concept_platform.entity.SysUser;
import com.example.concept_platform.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody SysUser loginUser) {
        if (loginUser.getUsername() == null || loginUser.getPassword() == null) {
            return Result.error("Username and password are required");
        }
        
        SysUser user = sysUserService.login(loginUser.getUsername(), loginUser.getPassword());
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("Invalid username or password");
        }
    }

    @GetMapping("/experts")
    public Result<List<SysUser>> getExperts() {
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        query.eq("role", "EXPERT");
        List<SysUser> list = sysUserService.list(query);
        // Hide passwords
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }
}
