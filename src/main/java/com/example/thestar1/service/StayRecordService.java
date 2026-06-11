package com.example.thestar1.service;

import com.example.thestar1.dto.CheckInDTO;
import com.example.thestar1.entity.OrderListVO;
import com.example.thestar1.entity.OrderVO;
import com.example.thestar1.entity.RoomVO;
import com.example.thestar1.entity.StayRecordVO;
import com.example.thestar1.repository.OrderListRepository;
import com.example.thestar1.repository.RoomRepository;
import com.example.thestar1.repository.StayRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StayRecordService {

    private final RoomRepository roomRepository;
    private final StayRecordRepository stayRecordRepository;
    private final OrderListRepository orderListRepository;

    @Autowired
    public StayRecordService(RoomRepository roomRepository, StayRecordRepository stayRecordRepository, OrderListRepository orderListRepository) {
        this.roomRepository = roomRepository;
        this.stayRecordRepository = stayRecordRepository;
        this.orderListRepository = orderListRepository;
    }

    @Transactional
    public void checkIn(Integer employeeId, CheckInDTO dto) {

        Integer orderListId = dto.getOrderListId();
        OrderListVO orderList = orderListRepository.findById(orderListId)
                .orElseThrow(() -> new IllegalArgumentException("明細不存在"));

        OrderVO order = orderList.getOrdervo();
        if (order.getOrderStatus() != 1) {
            throw new IllegalStateException("訂單非以付款，無法checkin");
        }


        int fullyBooked = stayRecordRepository.countByOrderListvo(orderList);
        if (fullyBooked >= orderList.getQuantity()) {
            throw new IllegalStateException("此明細以配滿房間數");
        }

        RoomVO room = roomRepository.findByRoomId(dto.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("房間不存在");
        } else if (!room.getRoomTypeId().equals(orderList.getRoomTypeId())) {
            throw new IllegalStateException("此房型不正確");
        } else if (room.getRoomStatus() == 1) {
            throw new IllegalStateException("此房間以有人入住");
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

}
