package com.example.thestar1.service;


import com.example.thestar1.repository.OrderRepository;
import com.example.thestar1.repository.RoomTypeRepository;
import com.example.thestar1.service.OrderQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
public class GetMemberDetailTest {

    @Autowired private OrderQueryService orderQueryService;   // 換成你 findOrderDetail 所在的 service
    @Autowired private OrderRepository orderRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;


    @Test
    void 會員查詢明細(){

        Integer memberA = 1/* 建單時用的會員 */;
        Integer memberB = memberA + 1;   // 隨便一個不同的會員
        Integer orderId =1;
// 正向：主人查得到
        assertFalse(orderQueryService.findMemberOrderDetail(memberA, orderId).isEmpty());

// 反向：別人被擋
        assertThrows(IllegalArgumentException.class,
                () -> orderQueryService.findMemberOrderDetail(memberB, orderId));
    }

}
