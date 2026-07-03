package com.thestar.content.repository;

import com.thestar.content.entity.ArticleVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<ArticleVO, Integer> {
}
