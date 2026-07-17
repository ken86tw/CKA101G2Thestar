package com.thestar.room.controller;


import com.thestar.room.dto.AdminRoomInventoryDTO;
import com.thestar.room.dto.RoomInventoryDTO;
import com.thestar.room.service.RoomInventoryQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/find")
public class RoomInventoryController {


    private final RoomInventoryQueryService roomInventoryQueryService;

    public RoomInventoryController(RoomInventoryQueryService roomInventoryQueryService) {
        this.roomInventoryQueryService = roomInventoryQueryService;
    }

    @GetMapping("/room")
    public ResponseEntity<List<RoomInventoryDTO>> checkRoomInventory(@RequestParam LocalDate checkInDate,
                                                                     @RequestParam LocalDate checkOutDate){

       return ResponseEntity.ok().body(roomInventoryQueryService.findAllAvailableRoom(checkInDate, checkOutDate));
    }


    @GetMapping("/admin/room")
    public ResponseEntity<List<AdminRoomInventoryDTO>> checkAdminRoomInventory(@RequestParam(required = false) LocalDate date){
        return ResponseEntity.ok().body(roomInventoryQueryService.findAdminRoom(date));
    }
}
