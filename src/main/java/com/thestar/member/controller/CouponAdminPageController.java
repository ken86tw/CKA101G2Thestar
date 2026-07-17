package com.thestar.member.controller;

import com.thestar.employee.security.EmployeeUserDetails;
import com.thestar.member.dto.CouponAdminForm;
import com.thestar.member.service.MemberCouponService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
public class CouponAdminPageController {

    private final MemberCouponService memberCouponService;

    public CouponAdminPageController(MemberCouponService memberCouponService) {
        this.memberCouponService = memberCouponService;
    }

    @GetMapping
    public String list(
            Model model,
            @AuthenticationPrincipal EmployeeUserDetails principal,
            @RequestParam(required = false) Integer memberCouponFilterCouponId,
            @RequestParam(required = false) String memberCouponFilterName,
            @RequestParam(required = false) String memberCouponFilterEmail
    ) {
        model.addAttribute("coupons", memberCouponService.getAllCoupons());
        model.addAttribute("couponForm", new CouponAdminForm());
        model.addAttribute(
                "enabledMembers",
                memberCouponService.getEnabledMembersForCouponIssue()
        );
        model.addAttribute(
                "memberCoupons",
                memberCouponService.getMemberCouponsForAdmin(
                        memberCouponFilterCouponId,
                        memberCouponFilterName,
                        memberCouponFilterEmail
                )
        );
        model.addAttribute("memberCouponFilterCouponId", memberCouponFilterCouponId);
        model.addAttribute(
                "memberCouponFilterName",
                memberCouponFilterName == null ? "" : memberCouponFilterName.trim()
        );
        model.addAttribute(
                "memberCouponFilterEmail",
                memberCouponFilterEmail == null ? "" : memberCouponFilterEmail.trim()
        );
        model.addAttribute(
                "memberCouponFilterActive",
                memberCouponFilterCouponId != null
                        || (memberCouponFilterName != null
                        && !memberCouponFilterName.isBlank())
                        || (memberCouponFilterEmail != null
                        && !memberCouponFilterEmail.isBlank())
        );
        model.addAttribute(
                "birthdayCouponIssuable",
                memberCouponService.isBirthdayCouponIssuable()
        );
        model.addAttribute("currentEmployeeName", currentEmployeeName(principal));

        return "admin/coupons/list";
    }

    @PostMapping("/create")
    public String create(
            @ModelAttribute CouponAdminForm couponForm,
            RedirectAttributes redirectAttributes
    ) {
        try {
            memberCouponService.createCoupon(couponForm);
            redirectAttributes.addFlashAttribute("message", "優惠券新增完成");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{couponId}/update")
    public String update(
            @PathVariable Integer couponId,
            @ModelAttribute CouponAdminForm couponForm,
            RedirectAttributes redirectAttributes
    ) {
        try {
            memberCouponService.updateCoupon(couponId, couponForm);
            redirectAttributes.addFlashAttribute("message", "優惠券資料已更新");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{couponId}/delete")
    public String delete(
            @PathVariable Integer couponId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            memberCouponService.deleteCoupon(couponId);
            redirectAttributes.addFlashAttribute("message", "優惠券已刪除");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{couponId}/status")
    public String updateStatus(
            @PathVariable Integer couponId,
            @RequestParam boolean enabled,
            RedirectAttributes redirectAttributes
    ) {
        try {
            memberCouponService.updateCouponIssueStatus(couponId, enabled);
            redirectAttributes.addFlashAttribute(
                    "message",
                    enabled ? "優惠券已恢復發放" : "優惠券已暫停發放"
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{couponId}/issue/member")
    public String issueToMember(
            @PathVariable Integer couponId,
            @RequestParam Integer memberId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            memberCouponService.issueCouponToMember(couponId, memberId);
            redirectAttributes.addFlashAttribute("message", "優惠券已發送給指定會員");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{couponId}/issue/all")
    public String issueToAll(
            @PathVariable Integer couponId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            int issuedCount =
                    memberCouponService.issueCouponToAllEnabledMembers(couponId);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "全會員發放完成，共新增 " + issuedCount + " 張會員券"
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/birthday/issue-current-month")
    public String issueCurrentMonthBirthdayCoupons(
            RedirectAttributes redirectAttributes
    ) {
        try {
            int issuedCount = memberCouponService.issueCurrentMonthBirthdayCoupons();
            redirectAttributes.addFlashAttribute(
                    "message",
                    "本月生日券補發完成，共發放 " + issuedCount + " 張"
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    private String currentEmployeeName(EmployeeUserDetails principal) {
        if (principal == null || principal.getEmployee() == null) {
            return "後台員工";
        }
        return principal.getEmployee().getEmployeeName();
    }
}
