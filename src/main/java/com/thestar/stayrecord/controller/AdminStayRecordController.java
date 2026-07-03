package com.thestar.stayrecord.controller;


import com.thestar.stayrecord.dto.CheckInDTO;
import com.thestar.room.entity.RoomVO;
import com.thestar.stayrecord.dto.FindCheckInRoomDTO;
import com.thestar.stayrecord.entity.StayRecordVO;
import com.thestar.stayrecord.service.StayRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/thestar/admin/stayrecord")
public class AdminStayRecordController {

    private final StayRecordService stayRecordService;

    public AdminStayRecordController(StayRecordService stayRecordService) {
        this.stayRecordService = stayRecordService;
    }

    @PostMapping("/checkin")
    public ResponseEntity<String> checkIn(@RequestBody CheckInDTO dto, HttpSession session) {
        Integer employeeId = (Integer) session.getAttribute("loginEmployee");
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        stayRecordService.checkIn(employeeId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("check in OK");
    }

    @PostMapping("/checkout/{roomId}")
    public ResponseEntity<String> checkOut(@PathVariable Integer roomId, HttpSession session) {

        Integer employeeId = (Integer) session.getAttribute("loginEmployee");

        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        stayRecordService.checkOut(roomId, employeeId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("房號" + roomId + "退房成功");
    }

    @GetMapping("/find")
    public ResponseEntity<List<StayRecordVO>> searchStayRecord(@RequestParam(required = false) Integer roomId,
                                                 @RequestParam(required = false) String stayCustomer,
                                                 @RequestParam(required = false) LocalDate checkInTime,
                                                 @RequestParam(required = false) LocalDate checkOutTime,
                                                 HttpSession session) {
        Integer empolyeeId = (Integer) session.getAttribute("loginEmployee");

        if (empolyeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<StayRecordVO> list = stayRecordService.findStayRecord(roomId, stayCustomer, checkInTime, checkOutTime);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/checkin-order/{orderId}")
    public ResponseEntity<List<FindCheckInRoomDTO>> checkInLines(@PathVariable Integer orderId,
                                                                 HttpSession session) {
        Integer employeeId = (Integer) session.getAttribute("loginEmployee");
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(stayRecordService.findCheckInLines(orderId));
    }

    // 階段2:點某筆明細配房
    @GetMapping("/checkin-rooms/{orderListId}")
    public ResponseEntity<List<RoomVO>> checkInRooms(@PathVariable Integer orderListId,
                                                     HttpSession session) {
        Integer employeeId = (Integer) session.getAttribute("loginEmployee");
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(stayRecordService.findRoomsByOrderList(orderListId));
    }

    @GetMapping("/find/all")
    public ResponseEntity<List<StayRecordVO>> searchAllNotCheckOutRoom(HttpSession session){
        Integer employeeId = (Integer) session.getAttribute("loginEmployee");

        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(stayRecordService.findAllNotCheckOutRoom());
    }
}

