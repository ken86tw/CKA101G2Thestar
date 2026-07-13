package com.thestar.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity // 標記身份，Hibernate可執行裡面內容
@Table(name = "ROOM_TYPE") // 讀取對應Table
public class RoomTypeVO {

	@Id // 標記為主鍵
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment屬性
	@Column(name = "ROOM_TYPE_ID", updatable = false) // 對應欄位，主鍵不可修改
	private Integer roomTypeId;

	@NotBlank(message = "房型名稱不能為空") // 驗證輸入
	@Column(name = "ROOM_TYPE_NAME")
	private String roomTypeName;

	@NotNull(message = "數量不能為空")
	@Min(value = 0, message = "數量不能小於 0") // 禁止負數
	@Digits(integer = 2, fraction = 0, message = "數量必須是整數") // 限制只能是整數，且不超過設定的2位數
	@Column(name = "ROOM_TYPE_AMOUNT")
	private Integer roomTypeAmount;

	@NotBlank(message = "房型說明不能為空")
	@Size(max = 1000, message = "房型說明不能超過設定的字數")
	@Column(name = "ROOM_TYPE_CONTENT")
	private String roomTypeContent;

	@Column(name = "ROOM_TYPE_STATUS", columnDefinition = "BIT(1)") //房型狀態設定
	private Boolean roomTypeStatus = false; // 預設為未啟用;

	@NotNull(message = "價格不能為空")
	@Min(value = 0, message = "價格不能為負數")
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
