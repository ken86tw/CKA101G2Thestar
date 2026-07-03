package com.example.thestar1.room.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ROOM_TYPE")
public class RoomTypeVO {

    // 主鍵:房型編號。IDENTITY = 交給 MySQL 的 AUTO_INCREMENT 產生。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROOM_TYPE_ID", updatable = false)
    private Integer roomTypeId;

    // 房型名稱,例如「雙人房」。
    @Column(name = "ROOM_TYPE_NAME")
    private String roomTypeName;

    // 這個房型「總共有幾間」(雙人房 30 / 四人房 15 / 總統套房 5)。
    // initInventory 建某一天的庫存列時,TOTAL_AMOUNT 就從這欄帶。
    @Column(name = "ROOM_TYPE_AMOUNT")
    private Integer roomTypeAmount;

    // 房型說明文字。
    @Column(name = "ROOM_TYPE_CONTENT")
    private String roomTypeContent;

    // 房型狀態(1 = 啟用之類),型別用 Byte 對應 DB 的 TINYINT。
    @Column(name = "ROOM_TYPE_STATUS")
    private Boolean roomTypeStatus;

    // 每晚單價。createOrder 算錢就是查這欄。
    @Column(name = "ROOM_TYPE_PRICE")
    private Integer roomTypePrice;

    public RoomTypeVO() {
        super();
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

    public Integer getRoomTypeAmount() {
        return roomTypeAmount;
    }

    public void setRoomTypeAmount(Integer roomTypeAmount) {
        this.roomTypeAmount = roomTypeAmount;
    }

    public String getRoomTypeContent() {
        return roomTypeContent;
    }

    public void setRoomTypeContent(String roomTypeContent) {
        this.roomTypeContent = roomTypeContent;
    }

    public Boolean getRoomTypeStatus() {
        return roomTypeStatus;
    }

    public void setRoomTypeStatus(Boolean roomTypeStatus) {
        this.roomTypeStatus = roomTypeStatus;
    }

    public Integer getRoomTypePrice() {
        return roomTypePrice;
    }

    public void setRoomTypePrice(Integer roomTypePrice) {
        this.roomTypePrice = roomTypePrice;
    }
}