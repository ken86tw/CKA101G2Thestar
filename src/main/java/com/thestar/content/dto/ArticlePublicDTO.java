package com.thestar.content.dto;

import com.thestar.content.entity.ArticleVO;

import java.time.LocalDateTime;

public class ArticlePublicDTO {

    private Integer articleId;
    private String title;
    private String category;
    private String contentPreview;
    private Integer viewCount;
    private LocalDateTime createAt;

    public static ArticlePublicDTO from(ArticleVO article) {
        ArticlePublicDTO dto = new ArticlePublicDTO();
        dto.articleId = article.getArticleId();
        dto.title = article.getTitle();
        dto.category = article.getCategory();
        dto.contentPreview = preview(article.getContent());
        dto.viewCount = article.getViewCount();
        dto.createAt = article.getCreateAt();
        return dto;
    }

    private static String preview(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 120 ? content.substring(0, 120) + "…" : content;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }
}
