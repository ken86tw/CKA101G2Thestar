package com.example.thestar1.stayrecord.service;

import com.example.thestar1.order.service.OrderService;

import com.example.thestar1.room.repository.RoomTypeRepository;
import com.example.thestar1.stayrecord.dto.CheckInDTO;
import com.example.thestar1.order.entity.OrderListVO;
import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.room.entity.RoomVO;
import com.example.thestar1.stayrecord.dto.FindCheckInRoomDTO;
import com.example.thestar1.stayrecord.entity.StayRecordVO;
import com.example.thestar1.order.repository.OrderListRepository;
import com.example.thestar1.room.repository.RoomRepository;
import com.example.thestar1.stayrecord.repository.StayRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StayRecordService {

    private final RoomRepository roomRepository;
    private final StayRecordRepository stayRecordRepository;
    private final OrderListRepository orderListRepository;
    private final OrderService orderService;
    private final RoomTypeRepository roomTypeRepository;

    @Autowired
    public StayRecordService(RoomRepository roomRepository, StayRecordRepository stayRecordRepository,
                             OrderListRepository orderListRepository, OrderService orderService,
                             RoomTypeRepository roomTypeRepository) {
        this.roomRepository = roomRepository;
        this.stayRecordRepository = stayRecordRepository;
        this.orderListRepository = orderListRepository;
        this.orderService = orderService;
        this.roomTypeRepository = roomTypeRepository;
    }

    @Transactional
    public void checkIn(Integer employeeId, CheckInDTO dto) {

        Integer orderListId = dto.getOrderListId();
        OrderListVO orderList = orderListRepository.findById(orderListId)
                .orElseThrow(() -> new IllegalArgumentException("明細不存在"));

        OrderVO order = orderList.getOrdervo();
        if (order.getOrderStatus() != 1) {
            throw new IllegalStateException("訂單非已付款，無法checkin");
        }

        //用stayrecord表格裡面的orderList外鍵查詢有幾筆對應住宿紀錄
        int fullyBooked = stayRecordRepository.countByOrderListvo(orderList);
        if (fullyBooked >= orderList.getQuantity()) {
            throw new IllegalStateException("此明細已配滿房間數");
        }

        RoomVO room = roomRepository.findByRoomId(dto.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("房間不存在");
        } else if (!room.getRoomTypeId().equals(orderList.getRoomTypeId())) {
            throw new IllegalStateException("此房型不正確");
        } else if (room.getRoomStatus() == 1) {
            throw new IllegalStateException("此房間已有人入住");
        } else if (room.getRoomSwitchStatus() == false) {
            throw new IllegalStateException("此房間停用中");
        } else {
            room.setRoomStatus((byte) 1);
        }


        StayRecordVO stay = new StayRecordVO();
        stay.setCheckInEmployeeId(employeeId);
        stay.setRoomId(dto.getRoomId());
        stay.setOrderListvo(orderList);
        stay.setStayCustomer(dto.getStayCustomer());
        stayRecordRepository.save(stay);

    }


    @Transactional
    public StayRecordVO checkOut(Integer roomId, Integer employeeId) {

        //先用客人房號找出住宿明細
        StayRecordVO stay = stayRecordRepository.findByRoomIdAndCheckOutTimeIsNull(roomId);
        if (stay == null) {
            throw new IllegalStateException("查無房號");
        }
        stay.setCheckOutTime(LocalDateTime.now());
        stay.setCheckOutEmployeeId(employeeId);
        //用房號建立一個room物件
        RoomVO room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("房間資料不存在"));
        if (room.getRoomStatus() == 0) {
            throw new IllegalStateException("房間未入住");
        }
        //確認後退房
        room.setRoomStatus((byte) 0);

        roomRepository.save(room);
        //確認未退房的房間為零以及總訂房間數跟住宿紀錄總量是相同的確保該住的都有住該退的都退了
        int orderId = stay.getOrderListvo().getOrdervo().getOrderId();
        int notCheckOutRooms = stayRecordRepository.countByOrderListvoOrdervoOrderIdAndCheckOutTimeIsNull(orderId);
        int totalBookedRooms = orderListRepository.sumQtyByOrderId(orderId);
        int totalStayRecord = stayRecordRepository.countByOrderListvoOrdervoOrderId(orderId);

        if (notCheckOutRooms == 0 && totalBookedRooms == totalStayRecord) {
            orderService.completeOrder(orderId);
        }
        stayRecordRepository.save(stay);
        return stay;
    }


    public List<StayRecordVO> findStayRecord(Integer roomId, String stayCustomer, LocalDate checkInTime, LocalDate checkOutTime) {
        //如果是空值就放空值進去不是就將他改為當天開始跟結束的午夜
        LocalDateTime start = (checkInTime == null) ? null : checkInTime.atStartOfDay();
        LocalDateTime end = (checkOutTime == null) ? null : checkOutTime.plusDays(1).atStartOfDay();
        return stayRecordRepository.FrontSearchStayRecordVO(roomId, stayCustomer, start, end);

    }


    // 後台輸入訂單ID,列出這張訂單每個房型「訂幾間/已入住/還剩幾間」,給配房用
    @Transactional(readOnly = true)
    public List<FindCheckInRoomDTO> findCheckInLines(Integer orderId) {

        // 用訂單ID撈出這張訂單的所有明細列(一個房型一列)
        List<OrderListVO> lines = orderListRepository.findByOrdervoOrderId(orderId);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("訂單不存在或無明細");
        }
        List<FindCheckInRoomDTO> result = new ArrayList<>();
        for (OrderListVO list : lines) {
            int checkedIn = stayRecordRepository.countByOrderListvo(list);

            // 把房型ID換成房型名稱給畫面顯示
            String name = roomTypeRepository.findById(list.getRoomTypeId())
                    .orElseThrow().getRoomTypeName();

            // 裝進DTO:剩餘 = 訂房數 - 已入住
            FindCheckInRoomDTO dto = new FindCheckInRoomDTO();
            dto.setOrderListId(list.getOrderListId());
            dto.setRoomTypeName(name);
            dto.setQuantity(list.getQuantity());
            dto.setCheckedIn(checkedIn);
            dto.setRemaining(list.getQuantity() - checkedIn);
            result.add(dto);
        }
        return result;
    }

    // 點某筆明細「配房」時,撈出該房型的全部房間(含入住中/停用)
    @Transactional(readOnly = true)
    public List<RoomVO> findRoomsByOrderList(Integer orderListId) {

        // 先用明細ID找出它是哪個房型
        OrderListVO orderList = orderListRepository.findById(orderListId)
                .orElseThrow(() -> new IllegalArgumentException("明細不存在"));

        // 回該房型所有房間,依房號排序
        return roomRepository.findByRoomTypeIdOrderByRoomId(orderList.getRoomTypeId());
    }

    @Transactional(readOnly = true)
    public List<StayRecordVO> findAllNotCheckOutRoom(){

        return stayRecordRepository.findByCheckOutTimeIsNullOrderByOrderListvoOrdervoOrderIdDesc();
    }
}

