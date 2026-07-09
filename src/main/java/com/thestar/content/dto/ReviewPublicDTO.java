package com.thestar.content.dto;

import com.thestar.content.entity.ReviewVO;

import java.time.LocalDateTime;

public class ReviewPublicDTO {

    private Integer reviewId;
    private Integer memberId;
    private String content;
    private LocalDateTime createdAt;

    public static ReviewPublicDTO from(ReviewVO review) {
        ReviewPublicDTO dto = new ReviewPublicDTO();
        dto.reviewId = review.getReviewId();
        dto.memberId = review.getMemberId();
        dto.content = review.getContent();
        dto.createdAt = review.getCreatedAt();
        return dto;
    }

    public Integer getReviewId() {
        return reviewId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
