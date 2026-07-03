package com.example.thestar1.content.repository;

import com.example.thestar1.content.entity.NewsVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<NewsVO, Integer> {
}
