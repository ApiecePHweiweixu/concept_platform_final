package com.example.concept_platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.concept_platform.entity.Review;
import com.example.concept_platform.entity.vo.ReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
    @Select("SELECT r.review_id, r.project_id, r.status, r.score, r.comments, " +
            "p.project_name, p.tech_domain, p.attachment_url, u.real_name as applicant_name " +
            "FROM review r " +
            "LEFT JOIN project p ON r.project_id = p.project_id " +
            "LEFT JOIN sys_user u ON p.applicant_id = u.user_id " +
            "WHERE r.expert_id = #{expertId} " +
            "ORDER BY r.status ASC, r.review_time DESC")
    List<ReviewVO> selectReviewVOList(Integer expertId);
}
