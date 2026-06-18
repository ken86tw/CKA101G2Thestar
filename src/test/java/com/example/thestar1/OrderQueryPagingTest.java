package com.example.thestar1;

import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.order.repository.OrderRepository;
import com.example.thestar1.order.service.OrderQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderQueryPagingTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional
        // 測完自動 rollback
    void 分頁_每頁五筆_應正確切頁() {
        Integer memberId = 1/* 填一個 DB 真實存在的會員 id */;
        byte status = 0;   // PENDING

        // 塞 6 筆「同會員、同狀態」的訂單
        for (int i = 0; i < 6; i++) {
            OrderVO o = new OrderVO();
            o.setMemberId(memberId);
            o.setCheckInDate(LocalDate.of(2026,9,3));
            o.setCheckOutDate(LocalDate.of(2026,9,3));
            o.setOrderStatus(status);
            o.setDiscountAmount(0);
            o.setPaidAmount(1000);
            o.setTotalAmount(1000);
            o.setPaymentMethod((byte) 1);   // NOT NULL，隨便給個合法值
            // merchantTradeNo 有 UNIQUE 限制，每筆要不一樣
            o.setMerchantTradeNo(o.getMerchantTradeNo());
            orderRepository.save(o);
        }

        // 第一頁
        Page<OrderVO> page0 = orderQueryService.findMemberOrder(memberId, status, 0, 5);
        assertEquals(5, page0.getContent().size());     // 這頁拿到 5 筆
        assertTrue(page0.getTotalElements() >= 6);      // 總數至少 6
        assertTrue(page0.getTotalPages() >= 2);         // 至少兩頁

        // 第二頁
        Page<OrderVO> page1 = orderQueryService.findMemberOrder(memberId, status, 1, 5);
        assertFalse(page1.getContent().isEmpty());      // 第二頁有資料

        // 兩頁不重複（用 orderId 比對）
        Set<Integer> ids0 = page0.getContent().stream()
                .map(OrderVO::getOrderId)
                .collect(Collectors.toSet());
        assertTrue(page1.getContent().stream()
                .noneMatch(o -> ids0.contains(o.getOrderId())));
    }
}
