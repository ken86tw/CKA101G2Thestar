package com.thestar.content.controller;

import com.thestar.content.dto.ArticlePublicDTO;
import com.thestar.content.dto.ReviewPublicDTO;
import com.thestar.content.entity.ReviewVO;
import com.thestar.content.service.ContentAdminService;
import com.thestar.member.entity.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/thestar/content")
public class ContentPublicController {

    private final ContentAdminService contentAdminService;

    public ContentPublicController(ContentAdminService contentAdminService) {
        this.contentAdminService = contentAdminService;
    }

    @GetMapping("/news")
    public List<ArticlePublicDTO> latestNews() {
        return contentAdminService.findLatestNews().stream()
                .map(ArticlePublicDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/articles")
    public Page<ArticlePublicDTO> articles(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "6") int size) {
        return contentAdminService.findPublishedArticles(PageRequest.of(page, size))
                .map(ArticlePublicDTO::from);
    }

    @GetMapping("/articles/{id}")
    public ArticlePublicDTO articleDetail(@PathVariable Integer id) {
        return ArticlePublicDTO.fromDetail(contentAdminService.findArticle(id));
    }

    @GetMapping("/articles/{id}/reviews")
    public List<ReviewPublicDTO> reviews(@PathVariable Integer id) {
        return contentAdminService.findReviewsForArticle(id).stream()
                .map(ReviewPublicDTO::from)
                .collect(Collectors.toList());
    }

    @PostMapping("/articles/{id}/reviews")
    public ResponseEntity<ReviewPublicDTO> addReview(@PathVariable Integer id,
                                                      @RequestBody Map<String, String> body,
                                                      HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ReviewVO review = contentAdminService.createReview(id, member.getMemberId(), body.get("content"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewPublicDTO.from(review));
    }
}
