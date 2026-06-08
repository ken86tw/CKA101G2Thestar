package com.example.thestar1.service;

import com.example.thestar1.dto.CreateRoomOrderDTO;
import com.example.thestar1.entity.OrderListVO;
import com.example.thestar1.entity.OrderVO;
import com.example.thestar1.entity.RoomTypeVO;
import com.example.thestar1.repository.OrderRepository;
import com.example.thestar1.repository.RoomInventoryRepository;
import com.example.thestar1.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, RoomInventoryRepository roomInventoryRepository, RoomTypeRepository roomTypeRepository) {
        this.orderRepository = orderRepository;
        this.roomInventoryRepository = roomInventoryRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    @Transactional
    public OrderVO createOrder(Integer memberId, CreateRoomOrderDTO dto) {

        LocalDate checkInDate = dto.getCheckInDate();
        LocalDate checkOutDate = dto.getCheckOutDate();


        if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("入住/退房日期不正確");
        }
        if (dto.getRooms() == null || dto.getRooms().isEmpty()) {
            throw new IllegalArgumentException("沒有選擇任何房型");
        }

        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();

        int totalAmount = 0;
        List<OrderListVO> items = new ArrayList<>();
        for (CreateRoomOrderDTO.RoomItem item : dto.getRooms()) {
            Integer roomTypeId = item.getRoomTypeId();
            int qty = item.getQty();

            RoomTypeVO roomType = roomTypeRepository.findById(roomTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("房型錯誤"));

            int price = roomType.getRoomTypePrice();
            int subtotal = price * qty * (int) nights;
            totalAmount += subtotal;

            OrderListVO listVO = new OrderListVO();
            listVO.setRoomTypeId(roomTypeId);
            listVO.setQuantity(qty);
            listVO.setRoomPrice(price);
            listVO.setSubtotal(subtotal);
            items.add(listVO);

        }

        List<DailyBooking> dailyBookings = new ArrayList<>();
        for (CreateRoomOrderDTO.RoomItem item : dto.getRooms()) {
            for (long i = 0; i < nights; i++) {
                LocalDate date = checkInDate.plusDays(i);
                dailyBookings.add(new DailyBooking(item.getRoomTypeId(), date, item.getQty()));
            }
        }

        dailyBookings.sort(Comparator.comparing((DailyBooking d) -> d.roomTypeId).thenComparing((DailyBooking d) -> d.date));

        for (DailyBooking d : dailyBookings) {
            roomInventoryRepository.initInventory(d.date, d.roomTypeId);
            int row = roomInventoryRepository.bookRooms(d.date, d.roomTypeId, d.qty);
            if (row == 0) {
                throw new IllegalStateException("房型" +  d.roomTypeId  + "於"  + d.date + "庫存不足，無法完成訂房");
            }
        }

        OrderVO ordervo = new OrderVO();
        ordervo.setMemberId(memberId);
        ordervo.setCouponId(dto.getCouponId());
        ordervo.setOrderStatus((byte)0);
        ordervo.setCheckInDate(checkInDate);
        ordervo.setCheckOutDate(checkOutDate);
        ordervo.setTotalAmount(totalAmount);
        ordervo.setDiscountAmount(0);
        ordervo.setPaidAmount(0);
        ordervo.setMerchantTradeNo(generateMerchantTradeNo());

        for(OrderListVO listVO : items){
            ordervo.addOrderList(listVO);
        }

        return orderRepository.save(ordervo);


    }

    private String generateMerchantTradeNo(){
        long ms = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000,10000);
        return "TS" + ms + random;
    }

    private static class DailyBooking {
        final Integer roomTypeId;
        final LocalDate date;
        final int qty;

        DailyBooking(Integer roomTypeId, LocalDate date, int qty) {
            this.roomTypeId = roomTypeId;
            this.date = date;
            this.qty = qty;
        }
    }
}
