package com.cjh.backend.dto.Comment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentListDto {

    private Long id;

    private Long userId;

    private String nickname;      // 评价人昵称（快照）

    private String avatar;        // 评价人头像（快照）

    private Integer score;

    private String content;

    private LocalDateTime createTime;
}