package com.thestar.member.controller;

import com.thestar.employee.security.EmployeeUserDetails;
import com.thestar.member.service.MemberAdminService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/members")
public class MemberAdminPageController {

    private final MemberAdminService memberAdminService;

    public MemberAdminPageController(MemberAdminService memberAdminService) {
        this.memberAdminService = memberAdminService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) String memberEmail,
            @RequestParam(required = false) Byte status,
            Model model,
            @AuthenticationPrincipal EmployeeUserDetails principal
    ) {
        model.addAttribute(
                "members",
                memberAdminService.searchMembers(
                        memberName,
                        memberEmail,
                        status
                )
        );
        model.addAttribute("memberName", memberName == null ? "" : memberName);
        model.addAttribute("memberEmail", memberEmail == null ? "" : memberEmail);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentEmployeeName", currentEmployeeName(principal));

        return "admin/member/list";
    }

    @PostMapping("/{memberId}/status")
    public String updateStatus(
            @PathVariable Integer memberId,
            @RequestParam Byte newStatus,
            RedirectAttributes redirectAttributes
    ) {
        try {
            memberAdminService.updateStatus(memberId, newStatus);

            redirectAttributes.addFlashAttribute(
                    "message",
                    newStatus == MemberAdminService.STATUS_SUSPENDED
                            ? "會員已停權"
                            : "會員已恢復啟用"
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }

        return "redirect:/admin/members";
    }

    private String currentEmployeeName(EmployeeUserDetails principal) {
        if (principal == null || principal.getEmployee() == null) {
            return "後台員工";
        }
        return principal.getEmployee().getEmployeeName();
    }
}
