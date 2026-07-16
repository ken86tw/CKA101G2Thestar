package com.thestar.room.service;


import com.thestar.room.dto.AdminRoomInventoryDTO;
import com.thestar.room.dto.RoomInventoryDTO;
import com.thestar.room.entity.RoomInventoryVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.repository.RoomInventoryRepository;
import com.thestar.room.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomInventoryQueryService {
    private final RoomInventoryRepository roomInventoryRepository;
    private final RoomTypeRepository roomTypeRepository;

    public RoomInventoryQueryService(RoomInventoryRepository roomInventoryRepository, RoomTypeRepository roomTypeRepository) {
        this.roomInventoryRepository = roomInventoryRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomInventoryDTO> findAllAvailableRoom(LocalDate checkInDate, LocalDate checkOutDate) {

        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("入住日不可比退房日晚");
        }
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("入住日不能早於今天");
        }
        List<RoomTypeVO> roomTypeVO = roomTypeRepository.findAll();

        List<RoomInventoryVO> roomInventoryVO = roomInventoryRepository.
                findByIdInventoryDateGreaterThanEqualAndIdInventoryDateLessThan(checkInDate, checkOutDate);


        Map<Integer, List<RoomInventoryVO>> map = roomInventoryVO.stream().
                collect(Collectors.groupingBy(vo -> vo.getId().getRoomTypeId()));


        List<RoomInventoryDTO> result = new ArrayList<>();

        for (RoomTypeVO rt : roomTypeVO) {

            List<RoomInventoryVO> list = map.get(rt.getRoomTypeId());
            int remain;
            if (list == null) {

                remain = rt.getRoomTypeAmount();

            } else {

                remain = list.stream().
                        mapToInt(amount -> amount.getTotalCount() - amount.getBookedCount())
                        .min()
                        .getAsInt();
            }
            result.add(new RoomInventoryDTO(rt.getRoomTypeId(), rt.getRoomTypeName(), remain, rt.getRoomTypePrice()));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<AdminRoomInventoryDTO> findAdminRoom(LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        List<RoomTypeVO> type = roomTypeRepository.findAll();
        List<RoomInventoryVO> inventory = roomInventoryRepository
                .findByIdInventoryDateGreaterThanEqualAndIdInventoryDateLessThan(date, date.plusDays(31));


        Map<Integer, Map<LocalDate, RoomInventoryVO>> roomMap = new HashMap<>();

        List<AdminRoomInventoryDTO> result = new ArrayList<>();


        for (RoomInventoryVO vo : inventory) {
            Map<LocalDate, RoomInventoryVO> datamap = roomMap
                    .computeIfAbsent(vo.getId().getRoomTypeId(), k -> new HashMap<>());

            datamap.put(vo.getId().getInventoryDate(), vo);
        }
        for (RoomTypeVO roomTypeVO : type) {
            Map<LocalDate, RoomInventoryVO> datamap = roomMap.getOrDefault(roomTypeVO.getRoomTypeId(), Map.of());
            for (LocalDate d = date; d.isBefore(date.plusDays(31)); d = d.plusDays(1)) {

                int total;
                int booked;
                int remain;
                RoomInventoryVO inventoryVO = datamap.get(d);
                if (inventoryVO == null) {
                    total = roomTypeVO.getRoomTypeAmount();
                    booked = 0;
                    remain = roomTypeVO.getRoomTypeAmount();
                } else {
                    total = inventoryVO.getTotalCount();
                    booked = inventoryVO.getBookedCount();
                    remain = total - booked;
                }
                result.add(new AdminRoomInventoryDTO(roomTypeVO.getRoomTypeId(), roomTypeVO.getRoomTypeName(), total, booked, remain, d));
            }
        }


        return result;
    }
}
