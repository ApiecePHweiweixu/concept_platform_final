package com.example.concept_platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * System User Entity
 */
@Data
@TableName("sys_user")
public class SysUser {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private String username;

    private String password;

    private String realName;

    private String field;

    private String role; // APPLICANT, EXPERT, ADMIN

    private LocalDateTime createdAt;
}

