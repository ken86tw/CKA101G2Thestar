package com.example.thestar1.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ROOM")
public class RoomVO {

    @Id
    @Column(name = "ROOM_ID")
    private Integer roomId;                 // 房號，後台人為指定，不自增

    @Column(name = "ROOM_TYPE_ID", nullable = false)
    private Integer roomTypeId;             // 房型 ID，純欄位，配房時比對用

    @Column(name = "ROOM_STATUS")
    private Byte roomStatus = (byte) 0;     // 0 空閒、1 入住中

    @Column(name = "ROOM_SWITCH_STATUS")
    private Boolean roomSwitchStatus = true; // true 啟用、false 停用

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public Byte getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(Byte roomStatus) {
        this.roomStatus = roomStatus;
    }

    public Boolean getRoomSwitchStatus() {
        return roomSwitchStatus;
    }

    public void setRoomSwitchStatus(Boolean roomSwitchStatus) {
        this.roomSwitchStatus = roomSwitchStatus;
    }
}