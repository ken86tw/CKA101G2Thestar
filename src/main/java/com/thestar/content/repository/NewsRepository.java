package com.thestar.content.repository;

import com.thestar.content.entity.NewsVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<NewsVO, Integer> {
}
