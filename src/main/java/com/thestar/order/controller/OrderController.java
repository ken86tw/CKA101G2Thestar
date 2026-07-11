package com.thestar.order.controller;


import com.thestar.order.dto.CreateRoomOrderDTO;
import com.thestar.order.dto.OrderDetailDTO;
import com.thestar.member.entity.MemberVO;
import com.thestar.order.entity.OrderVO;
import com.thestar.order.service.OrderQueryService;
import com.thestar.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/thestar/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public OrderController(OrderService orderService, OrderQueryService orderQueryService,
                           SimpMessagingTemplate simpMessagingTemplate) {
        this.orderService = orderService;
        this.orderQueryService = orderQueryService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderVO> createOrder(@RequestBody CreateRoomOrderDTO dto, HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        OrderVO order = orderService.createOrder(member.getMemberId(),dto);
        simpMessagingTemplate.convertAndSend("/topic/orders",(Object)Map.of("event","created"));
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
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

        //廣播要放在service之後 交易commit完員工重查才查得到新退款單
        simpMessagingTemplate.convertAndSend("/topic/refunds", (Object) Map.of("event", "created"));

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