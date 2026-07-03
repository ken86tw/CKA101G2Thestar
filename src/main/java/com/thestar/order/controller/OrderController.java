package com.example.thestar1.order.controller;


import com.example.thestar1.order.dto.CreateRoomOrderDTO;
import com.example.thestar1.order.dto.OrderDetailDTO;
import com.example.thestar1.member.entity.MemberVO;
import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.order.service.OrderQueryService;
import com.example.thestar1.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/thestar/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;

    public OrderController(OrderService orderService, OrderQueryService orderQueryService) {
        this.orderService = orderService;
        this.orderQueryService = orderQueryService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderVO> createOrder(@RequestBody CreateRoomOrderDTO dto, HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(member.getMemberId(), dto));
    }


    //contentType要送text送json的話會整個進去
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Integer orderId,
                                              @RequestBody String reason,
                                              HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer memberId = member.getMemberId();


        orderService.cancelOrder(memberId, orderId, reason);

        return ResponseEntity.ok("訂單" + orderId + "取消訂單成功");
    }

    @GetMapping("/member/order")
    public ResponseEntity<Page<OrderVO>> memberFindOrder(@RequestParam Byte orderStatus,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute("loginMember");

        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer memberId = member.getMemberId();

        return ResponseEntity.ok(orderQueryService.findMemberOrder(memberId, orderStatus, page, size));
    }

    @GetMapping("/member/order/detail/{orderId}")
    public ResponseEntity<List<OrderDetailDTO>> memberFindOrderList(@PathVariable Integer orderId,
                                                                    HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer memberId = member.getMemberId();

        return ResponseEntity.ok(orderQueryService.findMemberOrderDetail(memberId, orderId));
    }

}