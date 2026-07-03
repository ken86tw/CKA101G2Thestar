package com.example.thestar1.content.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ARTICLE")
public class ArticleVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ARTICLE_ID")
    private Integer articleId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "VIEW_COUNT")
    private Integer viewCount;

    @Column(name = "STATUS")
    private Byte status;

    @Column(name = "CONTENT")
    private String content;

    @Lob
    @Column(name = "COVER_IMAGE", columnDefinition = "LONGBLOB")
    private byte[] coverImage;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CREATE_AT", insertable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "UPDATE_AT", insertable = false, updatable = false)
    private LocalDateTime updateAt;

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(byte[] coverImage) {
        this.coverImage = coverImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }
}
