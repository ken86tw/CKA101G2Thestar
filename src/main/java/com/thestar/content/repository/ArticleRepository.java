package com.thestar.content.repository;

import com.thestar.content.entity.ArticleVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<ArticleVO, Integer> {

    List<ArticleVO> findFirst5ByStatusOrderByCreateAtDesc(Byte status);
}
