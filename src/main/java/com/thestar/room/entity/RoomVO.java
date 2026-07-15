package com.thestar.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ROOM")
public class RoomVO {

	@Id
	@Column(name = "ROOM_ID", updatable = false)
	@NotNull(message = "房間編號不能為空")
	@Digits(integer = 4, fraction = 0, message = "房間編號必須是數字，且長度不可超過 4 位")
	@Min(value = 100, message = "房間編號至少需為 100")
	@Max(value = 9999, message = "房間編號不得大於 9999")
	private Integer roomId;

	@Column(name = "ROOM_TYPE_ID", nullable = false)
	private Integer roomTypeId;

	@Column(name = "ROOM_STATUS", columnDefinition = "TINYINT")
	private Byte roomStatus = (byte)0;

	public static final int STATUS_AVAILABLE = 0; // 未入住
	public static final int STATUS_OCCUPIED = 1; // 已入住
	public static final int STATUS_CLEANING = 2; // 待清潔

	@Column(name = "ROOM_SWITCH_STATUS", columnDefinition = "BIT(1)")
	private Boolean roomSwitchStatus = false; // 預設為未啟用

	public RoomVO() {
		super();
	}

	public Integer getRoomTypeId() {
	    return roomTypeId;
	}

	public void setRoomTypeId(Integer roomTypeId) {
	    this.roomTypeId = roomTypeId;
	}
	
	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

//	public RoomTypeVO getRoomTypeVO() {
//		return roomTypeVO;
//	}
//
//	public void setRoomTypeVO(RoomTypeVO roomTypeVO) {
//		this.roomTypeVO = roomTypeVO;
//	}

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
