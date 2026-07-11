package com.thestar.refund.controller;


import com.thestar.refund.dto.RefundDTO;
import com.thestar.refund.service.RefundService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/thestar/admin/refund")
public class AdminRefundController {

    private final RefundService refundService;


    public AdminRefundController(RefundService refundService) {

        this.refundService = refundService;
    }

    @GetMapping("/find")
    public ResponseEntity<List<RefundDTO>> findPendingRefundList(HttpSession session) {
        Integer employeeId = (Integer) session.getAttribute("loginEmployee");
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(refundService.findPendingRefunds());
    }

    @PostMapping("/process/{refundId}")
    public ResponseEntity<String> processRefund(@PathVariable Integer refundId, HttpSession session) {
        Integer employeeId = (Integer) session.getAttribute("loginEmployee");
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        refundService.processRefund(employeeId, refundId);

        return ResponseEntity.ok("退款編號" + refundId + "號退款成功");
    }
}
