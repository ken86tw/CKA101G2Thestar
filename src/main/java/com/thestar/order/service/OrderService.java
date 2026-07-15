package com.thestar.order.service;

import com.thestar.member.service.MemberCouponService;
import com.thestar.room.service.RedisRoomStock;

import com.thestar.order.dto.CreateRoomOrderDTO;
import com.thestar.order.entity.OrderListVO;
import com.thestar.order.entity.OrderVO;
import com.thestar.refund.entity.RefundListVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.order.repository.OrderRepository;
import com.thestar.refund.repository.RefundListRepository;
import com.thestar.room.repository.RoomInventoryRepository;
import com.thestar.room.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RefundListRepository refundListRepository;
    private final RedisRoomStock redisRoomStock;
    private final MemberCouponService memberCouponService;


    @Autowired
    public OrderService(OrderRepository orderRepository, RoomInventoryRepository roomInventoryRepository
            , RoomTypeRepository roomTypeRepository, RefundListRepository refundListRepository
            , RedisRoomStock redisRoomStock, MemberCouponService memberCouponService) {
        this.orderRepository = orderRepository;
        this.roomInventoryRepository = roomInventoryRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.refundListRepository = refundListRepository;
        this.redisRoomStock = redisRoomStock;
        this.memberCouponService = memberCouponService;
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
        //算幾晚
        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();

        //訂單總額
        int totalAmount = 0;

        //建立一個暫存明細集合
        List<OrderListVO> orderList = new ArrayList<>();

        //算出 單個房型的住房期間費用 加到暫存明細中.
        //在最後建立訂單時在將全部的暫存明細放進訂單vo的維持關聯
        for (CreateRoomOrderDTO.RoomItem item : dto.getRooms()) {

            Integer roomTypeId = item.getRoomTypeId();
            int qty = item.getQty();
            if (qty <= 0) {
                throw new IllegalArgumentException("錯誤數量，房型數量必須大於零");
            }

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
            orderList.add(listVO);

        }


        //使用內部類別建立一個這個訂單所有房型的每日住房清單 因為庫存資料庫是用房型跟天數作為雙主鍵
        //需要使用雙層迴圈將天數拆解成一天一天以加入庫存
        //接下來將日期,房型與數量加入內部類別建出的物件後塞進list集合裡用來訂房
        List<DailyBooking> dailyBookings = new ArrayList<>();
        for (CreateRoomOrderDTO.RoomItem item : dto.getRooms()) {
            for (long i = 0; i < nights; i++) {
                LocalDate date = checkInDate.plusDays(i);
                dailyBookings.add(new DailyBooking(item.getRoomTypeId(), date, item.getQty()));
            }
        }
        //排序房型與日期讓不同筆訂單都能照同一順序去鎖資料庫避免互等死鎖
        dailyBookings.sort(Comparator.comparing((DailyBooking d) -> d.roomTypeId)
                .thenComparing((DailyBooking d) -> d.date));

        //建立一個redis操作過訂單的集合
        List<DailyBooking> redisBooked = new ArrayList<>();

        //先讓redis操作庫存 如果失敗丟例外後catch回滾
        for (DailyBooking d : dailyBookings) {

            redisRoomStock.initRedisRoom(d.roomTypeId, d.date);

            if (!redisRoomStock.bookRedisRoom(d.roomTypeId, d.date, d.qty)) {

                for (DailyBooking b : redisBooked) {
                    redisRoomStock.releaseRoom(b.roomTypeId, b.date, b.qty);
                }
                throw new IllegalStateException("房型" + d.roomTypeId +
                        "於" + d.date + "庫存不足，無法完成訂房");
            }
            redisBooked.add(d);
        }

        try {
            //使用dailyBookings將存好的每日訂房資料一筆一筆扣進庫存 回傳0交易失敗回滾
            for (DailyBooking d : dailyBookings) {
                roomInventoryRepository.initInventory(d.date, d.roomTypeId);
                int row = roomInventoryRepository.bookRooms(d.date, d.roomTypeId, d.qty);
                if (row == 0) {
                    throw new IllegalStateException("房型" + d.roomTypeId +
                            "於" + d.date + "庫存不足，無法完成訂房");
                }
            }
            //訂房資料存進去後建立訂單
            OrderVO ordervo = new OrderVO();
            ordervo.setMemberId(memberId);
            ordervo.setMemberCouponId(dto.getMemberCouponId());
            ordervo.setOrderStatus((byte) 0);
            ordervo.setCheckInDate(checkInDate);
            ordervo.setCheckOutDate(checkOutDate);
            ordervo.setTotalAmount(totalAmount);
            ordervo.setDiscountAmount(resolveCouponDiscount(memberId, dto.getMemberCouponId(), totalAmount));
            ordervo.setPaidAmount(0);
            ordervo.setMerchantTradeNo(generateMerchantTradeNo());

            //將此訂單依房型暫存明細一筆筆存入orderListVO
            for (OrderListVO listVO : orderList) {
                ordervo.addOrderList(listVO);
            }

            return orderRepository.save(ordervo);

        } catch (RuntimeException e) {
            //交易失敗要手動回滾redis用redis已操作過的明細集合紀錄
            for (DailyBooking d : redisBooked) {
                redisRoomStock.releaseRoom(d.roomTypeId, d.date, d.qty);
            }
            throw e;
        }
    }

    //每次進結帳頁重發一個新的金流編號並存檔，避免重複結帳時被綠界檔
    @Transactional
    public String renewMerchantTradeNo(Integer orderId) {
        OrderVO order = orderRepository.findById(orderId).orElseThrow();
        String newNo = generateMerchantTradeNo();
        order.setMerchantTradeNo(newNo);
        orderRepository.save(order);
        return newNo;
    }

    //建立折價券方法
    private int resolveCouponDiscount(Integer memberId, Integer memberCouponId, int totalAmount) {
        if (memberCouponId == null) {
            return 0;
        }
        return memberCouponService.useCouponForRoomOrder(memberId, memberCouponId, totalAmount);
    }

    //建立此訂單金流編號
    private String generateMerchantTradeNo() {
        long ms = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "TS" + ms + random;
    }

    //建立一個用於扣庫存的內部類別
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

    @Transactional
    public void confirmOrder(String merChantTradeNo, Integer paidAmount,
                             Byte paymentMethod, String ecpayTradeNo) {
        int row = orderRepository.confirmOrderPayment(paidAmount,
                paymentMethod, merChantTradeNo, ecpayTradeNo);
        if (row == 0) {
            throw new IllegalStateException("訂單不存在或是已處理" + merChantTradeNo);
        }
    }

    @Scheduled(fixedDelay = 20000)
    @Transactional
    public void cleanExpiredOrder() {
        LocalDateTime time = LocalDateTime.now().minusSeconds(200);

        List<OrderVO> expiredOrdeList = orderRepository.findByOrderStatusAndCreatedTimeBefore((byte) 0, time);

        for (OrderVO expiredOrder : expiredOrdeList) {
            int updated = orderRepository.canceledOrderPayment(expiredOrder.getOrderId());
            if (updated == 0) {
                continue;
            }
            LocalDate checkInDate = expiredOrder.getCheckInDate();
            LocalDate checkOutDate = expiredOrder.getCheckOutDate();
            long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();

            //若是沒結帳將會還券
            memberCouponService.restoreCouponForUnpaidOrder(expiredOrder.getMemberCouponId());

            for (OrderListVO orderList : expiredOrder.getOrderList()) {
                Integer roomTypeId = orderList.getRoomTypeId();
                int qty = orderList.getQuantity();

                for (int i = 0; i < nights; i++) {
                    LocalDate date = checkInDate.plusDays(i);
                    roomInventoryRepository.releaseRoom(date, roomTypeId, qty);
                    redisRoomStock.releaseRoom(roomTypeId, date, qty);
                }
            }
        }
    }

    @Transactional
    public void completeOrder(Integer orderId) {

        int row = orderRepository.finishOrder(orderId);
        if (row == 0) {
            throw new IllegalStateException("訂單無法完成 OrderId =" + orderId);
        }
    }

    @Transactional

    public void cancelOrder(Integer memberId, Integer orderId, String reason) {

        OrderVO ordervo = orderRepository.findById(orderId).orElseThrow();

        if (!Objects.equals(memberId, ordervo.getMemberId())) {
            throw new IllegalArgumentException("無法修改其他會員訂單");
        }

        int row = orderRepository.customerCancelOrder(orderId);
        if (row == 0) {
            throw new IllegalArgumentException("訂單狀態非已付款,不能取消");
        }
        LocalDate checkInDate = ordervo.getCheckInDate();
        LocalDate checkOutDate = ordervo.getCheckOutDate();
        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();

        List<OrderListVO> orderList = ordervo.getOrderList();
        for (OrderListVO list : orderList) {
            Integer roomTypeId = list.getRoomTypeId();
            int qty = list.getQuantity();
            for (int i = 0; i < nights; i++) {
                LocalDate date = checkInDate.plusDays(i);
                roomInventoryRepository.releaseRoom(date, roomTypeId, qty);
                redisRoomStock.releaseRoom(roomTypeId, date, qty);
            }
        }
        RefundListVO refund = new RefundListVO();
        refund.setAmount(ordervo.getPaidAmount());
        refund.setRefundStatus((byte) 0);
        refund.setReason(reason);
        refund.setOrdervo(ordervo);
        refundListRepository.save(refund);
    }


}
