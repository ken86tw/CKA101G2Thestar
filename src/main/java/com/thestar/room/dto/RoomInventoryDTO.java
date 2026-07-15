package com.thestar.room.dto;

public class RoomInventoryDTO {

    private Integer roomTypeId;
    private String roomTypeName;
    private int amount;
    private int price;


    public RoomInventoryDTO(Integer roomTypeId, String roomTypename, int amount, int price) {
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypename;
        this.amount = amount;
        this.price = price;
    }

    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
