package com.thestar.refund.controller;


import com.thestar.refund.dto.RefundDTO;
import com.thestar.refund.service.RefundService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/thestar/admin/refund")
public class AdminRefundController {

    private final RefundService refundService;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public AdminRefundController(RefundService refundService, SimpMessagingTemplate simpMessagingTemplate) {

        this.refundService = refundService;
        this.simpMessagingTemplate = simpMessagingTemplate;
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
        Integer memberId = refundService.processRefund(employeeId, refundId);

        //廣播要放在service之後 交易commit完再通知 重查才查得到最新狀態
        simpMessagingTemplate.convertAndSend("/topic/refunds", (Object) Map.of("event", "processed"));
        simpMessagingTemplate.convertAndSend("/topic/member/" + memberId, (Object) Map.of("event", "refunded"));

        return ResponseEntity.ok("退款編號" + refundId + "號退款成功");
    }
}
