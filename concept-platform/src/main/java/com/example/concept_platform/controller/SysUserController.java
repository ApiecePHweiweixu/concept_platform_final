package com.example.concept_platform.controller;

import com.example.concept_platform.common.Result;
import com.example.concept_platform.entity.SysUser;
import com.example.concept_platform.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}

