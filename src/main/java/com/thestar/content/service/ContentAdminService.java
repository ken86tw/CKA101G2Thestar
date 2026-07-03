package com.thestar.content.service;

import com.thestar.content.entity.ArticleVO;
import com.thestar.content.entity.NewsVO;
import com.thestar.content.entity.ReviewVO;
import com.thestar.content.repository.ArticleRepository;
import com.thestar.content.repository.NewsRepository;
import com.thestar.content.repository.ReviewRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ContentAdminService {

    private final NewsRepository newsRepository;
    private final ArticleRepository articleRepository;
    private final ReviewRepository reviewRepository;

    public ContentAdminService(NewsRepository newsRepository, ArticleRepository articleRepository,
                               ReviewRepository reviewRepository) {
        this.newsRepository = newsRepository;
        this.articleRepository = articleRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<NewsVO> findAllNews() {
        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public NewsVO findNews(Integer id) {
        return newsRepository.findById(id).orElseThrow(() -> new NoSuchElementException("查無最新消息"));
    }

    public NewsVO saveNews(NewsVO news) {
        if (news.getTitle() == null || news.getTitle().isBlank()) {
            throw new IllegalArgumentException("標題為必填");
        }
        if (news.getContent() == null || news.getContent().isBlank()) {
            throw new IllegalArgumentException("內容為必填");
        }
        if (news.getViewCount() == null) {
            news.setViewCount(0);
        }
        if (news.getStatus() == null) {
            news.setStatus((byte) 1);
        }
        return newsRepository.save(news);
    }

    public void updateNewsStatus(Integer id, boolean published) {
        NewsVO news = findNews(id);
        news.setStatus((byte) (published ? 1 : 0));
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
