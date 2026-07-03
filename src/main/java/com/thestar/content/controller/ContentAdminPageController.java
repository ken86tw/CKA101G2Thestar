package com.thestar.content.controller;

import com.thestar.content.entity.ArticleVO;
import com.thestar.content.entity.NewsVO;
import com.thestar.content.service.ContentAdminService;
import com.thestar.employee.security.EmployeeUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/thestar/admin/content")
public class ContentAdminPageController {

    private final ContentAdminService contentAdminService;

    public ContentAdminPageController(ContentAdminService contentAdminService) {
        this.contentAdminService = contentAdminService;
    }

    @GetMapping("/news")
    public String newsList(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        addShell(model, principal);
        model.addAttribute("newsList", contentAdminService.findAllNews());
        return "admin/content/news-list";
    }

    @GetMapping("/news/add")
    public String newsAdd(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        addShell(model, principal);
        model.addAttribute("mode", "add");
        model.addAttribute("news", new NewsVO());
        return "admin/content/news-form";
    }

    @PostMapping("/news/add")
    public String newsCreate(@ModelAttribute NewsVO news, RedirectAttributes redirectAttributes) {
        try {
            contentAdminService.saveNews(news);
            redirectAttributes.addFlashAttribute("message", "新增最新消息成功");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/thestar/admin/content/news";
    }

    @GetMapping("/news/edit/{id}")
    public String newsEdit(@PathVariable Integer id, Model model,
                           @AuthenticationPrincipal EmployeeUserDetails principal) {
        addShell(model, principal);
        model.addAttribute("mode", "edit");
        model.addAttribute("news", contentAdminService.findNews(id));
        return "admin/content/news-form";
    }

    @PostMapping("/news/edit/{id}")
    public String newsUpdate(@PathVariable Integer id, @ModelAttribute NewsVO news,
                             RedirectAttributes redirectAttributes) {
        NewsVO old = contentAdminService.findNews(id);
        news.setNewsId(id);
        news.setViewCount(old.getViewCount());
        if (news.getStatus() == null) {
            news.setStatus(old.getStatus());
        }
        contentAdminService.saveNews(news);
        redirectAttributes.addFlashAttribute("message", "最新消息已更新");
        return "redirect:/thestar/admin/content/news";
    }

    @PostMapping("/news/{id}/toggle-status")
    public String newsToggle(@PathVariable Integer id, @RequestParam boolean published,
                             RedirectAttributes redirectAttributes) {
        contentAdminService.updateNewsStatus(id, published);
        redirectAttributes.addFlashAttribute("message", published ? "最新消息已發布" : "最新消息已下架");
        return "redirect:/thestar/admin/content/news";
    }

    @GetMapping("/article")
    public String articleList(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        addShell(model, principal);
        model.addAttribute("articles", contentAdminService.findAllArticles());
        return "admin/content/article-list";
    }

    @GetMapping("/article/add")
    public String articleAdd(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        addShell(model, principal);
        model.addAttribute("mode", "add");
        model.addAttribute("article", new ArticleVO());
        return "admin/content/article-form";
    }

    @PostMapping("/article/add")
    public String articleCreate(@ModelAttribute ArticleVO article,
                                @AuthenticationPrincipal EmployeeUserDetails principal,
                                RedirectAttributes redirectAttributes) {
        try {
            contentAdminService.saveArticle(article, principal.getEmployeeId());
            redirectAttributes.addFlashAttribute("message", "新增文章成功");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/thestar/admin/content/article";
    }

    @GetMapping("/article/edit/{id}")
    public String articleEdit(@PathVariable Integer id, Model model,
                              @AuthenticationPrincipal EmployeeUserDetails principal) {
        addShell(model, principal);
        model.addAttribute("mode", "edit");
        model.addAttribute("article", contentAdminService.findArticle(id));
        return "admin/content/article-form";
    }

    @PostMapping("/article/edit/{id}")
    public String articleUpdate(@PathVariable Integer id, @ModelAttribute ArticleVO article,
                                @AuthenticationPrincipal EmployeeUserDetails principal,
                                RedirectAttributes redirectAttributes) {
        ArticleVO old = contentAdminService.findArticle(id);
        article.setArticleId(id);
        article.setEmployeeId(old.getEmployeeId());
        article.setViewCount(old.getViewCount());
        article.setCoverImage(old.getCoverImage());
        if (article.getStatus() == null) {
            article.setStatus(old.getStatus());
        }
        contentAdminService.saveArticle(article, principal.getEmployeeId());
        redirectAttributes.addFlashAttribute("message", "文章已更新");
        return "redirect:/thestar/admin/content/article";
    }

    @PostMapping("/article/{id}/toggle-status")
    public String articleToggle(@PathVariable Integer id, @RequestParam boolean published,
                                RedirectAttributes redirectAttributes) {
        contentAdminService.updateArticleStatus(id, published);
        redirectAttributes.addFlashAttribute("message", published ? "文章已發布" : "文章已下架");
        return "redirect:/thestar/admin/content/article";
    }

    @GetMapping("/review")
    public String reviewList(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        addShell(model, principal);
        model.addAttribute("reviews", contentAdminService.findAllReviews());
        return "admin/content/review-list";
    }

    @PostMapping("/review/{id}/delete")
    public String reviewDelete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        contentAdminService.deleteReview(id);
        redirectAttributes.addFlashAttribute("message", "評論已刪除");
        return "redirect:/thestar/admin/content/review";
    }

    private void addShell(Model model, EmployeeUserDetails principal) {
        model.addAttribute("currentEmployeeName", principal.getEmployee().getEmployeeName());
        model.addAttribute("isSuperAdmin", principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
    }
}
