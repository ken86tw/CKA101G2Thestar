package com.example.thestar1.employee.controller;

import com.example.thestar1.employee.dto.EmployeeCreateDTO;
import com.example.thestar1.employee.dto.EmployeeUpdateDTO;
import com.example.thestar1.employee.dto.EmployeeViewDTO;
import com.example.thestar1.employee.entity.DepartmentVO;
import com.example.thestar1.employee.entity.EmployeeVO;
import com.example.thestar1.employee.entity.JobTitleVO;
import com.example.thestar1.employee.entity.RoleVO;
import com.example.thestar1.employee.repository.DepartmentRepository;
import com.example.thestar1.employee.repository.JobTitleRepository;
import com.example.thestar1.employee.security.EmployeeUserDetails;
import com.example.thestar1.employee.service.EmployeeService;
import com.example.thestar1.employee.service.RoleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 員工管理頁面（Thymeleaf 伺服端渲染，比照 spec.md 參考的 EMP3 CRUD 頁面設計：
 * list / add / edit 各自獨立頁面，表單送出後導回列表頁）。
 * JSON REST API（{@link EmployeeController}）維持不變，供程式化存取使用；
 * 這裡的頁面 Controller 直接呼叫同一組 Service，避免邏輯重複。
 */
@Controller
@RequestMapping("/thestar/admin/employee")
public class EmployeePageController {

    private final EmployeeService employeeService;
    private final RoleService roleService;
    private final DepartmentRepository departmentRepository;
    private final JobTitleRepository jobTitleRepository;

    public EmployeePageController(EmployeeService employeeService, RoleService roleService,
                                   DepartmentRepository departmentRepository, JobTitleRepository jobTitleRepository) {
        this.employeeService = employeeService;
        this.roleService = roleService;
        this.departmentRepository = departmentRepository;
        this.jobTitleRepository = jobTitleRepository;
    }

    @GetMapping("/list")
    public String list(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        List<EmployeeViewDTO> employees = employeeService.findAll().stream()
                .map(EmployeeViewDTO::from)
                .collect(Collectors.toList());
        model.addAttribute("employees", employees);
        model.addAttribute("departmentNames", lookupNames(departmentRepository.findAll(),
                DepartmentVO::getDepartmentId, DepartmentVO::getDepartmentName));
        model.addAttribute("jobTitleNames", lookupNames(jobTitleRepository.findAll(),
                JobTitleVO::getJobTitleId, JobTitleVO::getJobTitleName));
        model.addAttribute("currentEmployeeName", principal.getEmployee().getEmployeeName());
        model.addAttribute("isSuperAdmin",
                principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
        return "admin/employee/list";
    }

    @GetMapping("/add")
    public String addForm(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        model.addAttribute("mode", "add");
        model.addAttribute("employee", new EmployeeCreateDTO());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("jobTitles", jobTitleRepository.findAll());
        model.addAttribute("currentEmployeeName", principal.getEmployee().getEmployeeName());
        model.addAttribute("isSuperAdmin",
                principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
        return "admin/employee/form";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute EmployeeCreateDTO dto, RedirectAttributes redirectAttributes) {
        try {
            employeeService.create(dto);
            redirectAttributes.addFlashAttribute("message", "新增員工成功");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/thestar/admin/employee/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        EmployeeVO employee = employeeService.findById(id);

        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();
        dto.setDepartmentId(employee.getDepartmentId());
        dto.setEmployeeName(employee.getEmployeeName());
        dto.setPhone(employee.getPhone());
        dto.setAddress(employee.getAddress());
        dto.setJobTitleId(employee.getJobTitleId());
        dto.setGender(employee.getGender());

        model.addAttribute("mode", "edit");
        model.addAttribute("employeeId", id);
        model.addAttribute("employeeMail", employee.getEmployeeMail());
        model.addAttribute("employee", dto);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("jobTitles", jobTitleRepository.findAll());
        model.addAttribute("currentEmployeeName", principal.getEmployee().getEmployeeName());
        model.addAttribute("isSuperAdmin",
                principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
        return "admin/employee/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, @ModelAttribute EmployeeUpdateDTO dto,
                        RedirectAttributes redirectAttributes) {
        employeeService.update(id, dto);
        redirectAttributes.addFlashAttribute("message", "修改成功");
        return "redirect:/thestar/admin/employee/list";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Integer id, @RequestParam boolean enabled,
                                RedirectAttributes redirectAttributes) {
        employeeService.updateStatus(id, enabled);
        redirectAttributes.addFlashAttribute("message", enabled ? "已恢復在職" : "已設為離職");
        return "redirect:/thestar/admin/employee/list";
    }

    @GetMapping("/{id}/roles")
    public String rolesForm(@PathVariable Integer id, Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        EmployeeVO employee = employeeService.findById(id);
        Set<Integer> assignedRoleIds = employee.getRoles().stream().map(RoleVO::getRoleId).collect(Collectors.toSet());

        model.addAttribute("employee", EmployeeViewDTO.from(employee));
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("assignedRoleIds", assignedRoleIds);
        model.addAttribute("currentEmployeeName", principal.getEmployee().getEmployeeName());
        model.addAttribute("isSuperAdmin",
                principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
        return "admin/employee/roles";
    }

    @PostMapping("/{id}/roles")
    public String assignRoles(@PathVariable Integer id,
                               @RequestParam(required = false) List<Integer> roleIds,
                               RedirectAttributes redirectAttributes) {
        employeeService.assignRoles(id, roleIds == null ? List.of() : roleIds);
        redirectAttributes.addFlashAttribute("message", "角色指派已更新");
        return "redirect:/thestar/admin/employee/list";
    }

    private <T> java.util.Map<Integer, String> lookupNames(List<T> items,
                                                             java.util.function.Function<T, Integer> idFn,
                                                             java.util.function.Function<T, String> nameFn) {
        return items.stream().collect(Collectors.toMap(idFn, nameFn));
    }
}
