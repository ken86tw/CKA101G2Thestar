package com.thestar.room.dto;

import java.time.LocalDate;

public class AdminRoomInventoryDTO {

    private Integer roomTypeId;
    private String roomTypeName;
    private int totalAmount;
    private int bookedAmount;
    private int remainAmount;
    private LocalDate date;

    public AdminRoomInventoryDTO() {
    }

    public AdminRoomInventoryDTO(Integer roomTypeId, String roomTypeName, int totalAmount,
                                 int bookedAmount, int remainAmount, LocalDate date) {
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.totalAmount = totalAmount;
        this.bookedAmount = bookedAmount;
        this.remainAmount = remainAmount;
        this.date = date;
    }

    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public int getBookedAmount() {
        return bookedAmount;
    }

    public void setBookedAmount(int bookedAmount) {
        this.bookedAmount = bookedAmount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(int remainAmount) {
        this.remainAmount = remainAmount;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }
}
