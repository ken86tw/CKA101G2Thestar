package com.thestar.room.service;


import com.thestar.room.dto.RoomInventoryDTO;
import com.thestar.room.entity.RoomInventoryVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.repository.RoomInventoryRepository;
import com.thestar.room.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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
                result.add(new RoomInventoryDTO(rt.getRoomTypeId(),rt.getRoomTypeName(),remain,rt.getRoomTypePrice()));
        }

        return result;
    }
}
