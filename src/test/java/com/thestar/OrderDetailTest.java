package com.example.thestar1;

import com.example.thestar1.order.dto.OrderDetailDTO;
import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.order.entity.OrderListVO;
import com.example.thestar1.room.entity.RoomTypeVO;
import com.example.thestar1.order.repository.OrderRepository;
import com.example.thestar1.room.repository.RoomTypeRepository;
import com.example.thestar1.order.service.OrderQueryService;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OrderDetailTest {

    @Autowired private OrderQueryService orderQueryService;   // 換成你 findOrderDetail 所在的 service
    @Autowired private OrderRepository orderRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;
    @Autowired
    private EntityManager entityManager;
    @Test
    @Transactional   // 測完自動 rollback
    void 明細_應帶出房型名稱且單價用凍結價() {
        Integer memberId = 1 /* 填 DB 真實存在的會員 id */;

        // 拿一個真實存在的房型，順便取它的名稱當驗證基準
        RoomTypeVO type = roomTypeRepository.findAll().get(0);
        Integer roomTypeId = type.getRoomTypeId();
        String expectedName = type.getRoomTypeName();

        // 故意把明細單價設成一個不太可能跟現價相同的值，
        // 這樣若程式誤用 ROOM_TYPE 現價，assert 就會抓到
        int frozenPrice = 12345;
        int qty = 2;
        int subtotal = frozenPrice * qty;

        // 建訂單 + 一筆明細，用 addOrderList 同步雙向關聯，靠 cascade PERSIST 一起存
        OrderVO o = new OrderVO();
        o.setMemberId(memberId);
        o.setCheckInDate(LocalDate.of(2026,9,3));
        o.setCheckOutDate(LocalDate.of(2026,9,3));
        o.setOrderStatus((byte)1);
        o.setDiscountAmount(0);
        o.setPaidAmount(1000);
        o.setTotalAmount(1000);
        o.setPaymentMethod((byte) 1);   // NOT NULL，隨便給個合法值
        // merchantTradeNo 有 UNIQUE 限制，每筆要不一樣
        o.setMerchantTradeNo(o.getMerchantTradeNo());
        orderRepository.save(o);
        OrderListVO line = new OrderListVO();
        line.setRoomTypeId(roomTypeId);
        line.setQuantity(qty);
        line.setRoomPrice(frozenPrice);
        line.setSubtotal(subtotal);
        o.addOrderList(line);

        Integer orderId = o.getOrderId();
        entityManager.flush();   // 把 INSERT（含明細）真的寫進 DB
        entityManager.clear();
        // 查明細
        List<OrderDetailDTO> result = orderQueryService.findOrderDetail(orderId);

        assertEquals(1, result.size());
        OrderDetailDTO dto = result.get(0);
        assertEquals(expectedName, dto.getRoomTypeName());  // 名稱有從 ROOM_TYPE 帶出
        assertEquals(qty, dto.getQTY());
        assertEquals(subtotal, dto.getSubtotal());
        assertEquals(frozenPrice, dto.getRoomPrice());      // 關鍵：用明細凍結價，不是現價
    }
}