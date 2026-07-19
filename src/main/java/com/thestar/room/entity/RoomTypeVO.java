package com.thestar.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
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

	@Column(name = "ROOM_TYPE_AMOUNT")
	private Integer roomTypeAmount;

	@NotBlank(message = "房型說明不能為空")
	@Size(max = 1000, message = "房型說明不能超過設定的字數")
	@Column(name = "ROOM_TYPE_CONTENT")
	private String roomTypeContent;
	
	@NotNull(message = "人數不能為空")
    @Min(value = 1, message = "人數至少 1 人")
	@Max(value = 6, message = "人數最多 6 人")
	@Column(name = "CAPACITY", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer capacity;
	
	@Size(max = 255, message = "設施說明長度不能超過 255 個字")
	@Column(name = "AMENITIES", length = 255)
    private String amenities;
	

	@Column(name = "ROOM_TYPE_STATUS", columnDefinition = "BIT(1)") // 房型狀態設定
	private Boolean roomTypeStatus = false; // 預設為未啟用;

	@NotNull(message = "價格不能為空")
	@Min(value = 0, message = "價格不能為負數")
	@Column(name = "ROOM_TYPE_PRICE")
	private Integer roomTypePrice;

	// 給roomList.html使用的不存在欄位，用於前台抓取該房型第一張圖
	@Transient
	private RoomTypePhotoVO firstPhoto;

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
	
	public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
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

	// 給firstPhoto不存在的欄位使用
	public RoomTypePhotoVO getFirstPhoto() {
		return firstPhoto;
	}

	public void setFirstPhoto(RoomTypePhotoVO firstPhoto) {
		this.firstPhoto = firstPhoto;
	}

}
