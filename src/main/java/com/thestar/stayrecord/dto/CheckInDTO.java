package com.example.thestar1.stayrecord.dto;

public class CheckInDTO {

    private Integer orderListId;
    private Integer roomId;
    private String stayCustomer;

    public Integer getOrderListId() {
        return orderListId;
    }

    public void setOrderListId(Integer orderListId) {
        this.orderListId = orderListId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getStayCustomer() {
        return stayCustomer;
    }

    public void setStayCustomer(String stayCustomer) {
        this.stayCustomer = stayCustomer;
    }
}
