package com.example.thestar1.order.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateRoomOrderDTO {

    private Integer couponId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<RoomItem> rooms;

    public static class RoomItem {
        private Integer roomTypeId;
        private int qty;

        public Integer getRoomTypeId() {
            return roomTypeId;
        }

        public void setRoomTypeId(Integer roomTypeId) {
            this.roomTypeId = roomTypeId;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }


    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public List<RoomItem> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomItem> rooms) {
        this.rooms = rooms;
    }
}
