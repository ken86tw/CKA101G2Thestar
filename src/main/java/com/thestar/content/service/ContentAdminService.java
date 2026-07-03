package com.thestar.content.service;

import com.thestar.content.entity.ArticleVO;
import com.thestar.content.entity.ReviewVO;
import com.thestar.content.repository.ArticleRepository;
import com.thestar.content.repository.ReviewRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ContentAdminService {

    private final ArticleRepository articleRepository;
    private final ReviewRepository reviewRepository;

    public ContentAdminService(ArticleRepository articleRepository, ReviewRepository reviewRepository) {
        this.articleRepository = articleRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * 最新消息＝最新 5 篇已發布文章的內容，不足 5 篇就顯示現有的篇數；刪除最新消息即刪除對應的文章。
     */
    @Transactional(readOnly = true)
    public List<ArticleVO> findLatestNews() {
        return articleRepository.findFirst5ByStatusOrderByCreateAtDesc((byte) 1);
    }

    @Transactional(readOnly = true)
    public List<ArticleVO> findAllArticles() {
        return articleRepository.findAll(Sort.by(Sort.Direction.DESC, "createAt"));
    }

    @Transactional(readOnly = true)
    public ArticleVO findArticle(Integer id) {
        return articleRepository.findById(id).orElseThrow(() -> new NoSuchElementException("查無文章"));
    }

    public ArticleVO saveArticle(ArticleVO article, Integer fallbackEmployeeId) {
        if (article.getTitle() == null || article.getTitle().isBlank()) {
            throw new IllegalArgumentException("標題為必填");
        }
        if (article.getCategory() == null || article.getCategory().isBlank()) {
            throw new IllegalArgumentException("分類為必填");
        }
        if (article.getContent() == null || article.getContent().isBlank()) {
            throw new IllegalArgumentException("內容為必填");
        }
        if (article.getEmployeeId() == null) {
            article.setEmployeeId(fallbackEmployeeId);
        }
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        if (article.getStatus() == null) {
            article.setStatus((byte) 1);
        }
        if (article.getCoverImage() == null) {
            article.setCoverImage(new byte[0]);
        }
        return articleRepository.save(article);
    }

    public void updateArticleStatus(Integer id, boolean published) {
        ArticleVO article = findArticle(id);
        article.setStatus((byte) (published ? 1 : 0));
    }

    public void deleteArticle(Integer id) {
        if (!articleRepository.existsById(id)) {
            throw new NoSuchElementException("查無文章");
        }
        articleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReviewVO> findAllReviews() {
        return reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public void deleteReview(Integer id) {
        if (!reviewRepository.existsById(id)) {
            throw new NoSuchElementException("查無評論");
        }
        reviewRepository.deleteById(id);
    }
}
