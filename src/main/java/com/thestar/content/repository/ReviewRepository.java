package com.thestar.content.repository;

import com.thestar.content.entity.ReviewVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewVO, Integer> {

    List<ReviewVO> findByArticleIdOrderByCreatedAtDesc(Integer articleId);

    void deleteByArticleId(Integer articleId);
}
