package com.example.thestar1.content.repository;

import com.example.thestar1.content.entity.ArticleVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<ArticleVO, Integer> {
}
