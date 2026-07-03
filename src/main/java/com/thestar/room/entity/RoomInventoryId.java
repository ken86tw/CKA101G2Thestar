package com.example.thestar1.room.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;


@Embeddable
public class RoomInventoryId implements Serializable{
	
	
	@Column(name = "INVENTORY_DATE")
	private LocalDate inventoryDate;
	
	@Column(name="ROOM_TYPE_ID")
	private Integer roomTypeId;

	public RoomInventoryId() {
		super();
	}

	public RoomInventoryId(LocalDate inventoryDate, Integer roomTypeId) {
		super();
		this.inventoryDate = inventoryDate;
		this.roomTypeId = roomTypeId;
	}

	public LocalDate getInventoryDate() {
		return inventoryDate;
	}

	public void setInventoryDate(LocalDate inventoryDate) {
		this.inventoryDate = inventoryDate;
	}

	public Integer getRoomTypeId() {
		return roomTypeId;
	}

	public void setRoomTypeId(Integer roomTypeId) {
		this.roomTypeId = roomTypeId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(inventoryDate, roomTypeId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoomInventoryId other = (RoomInventoryId) obj;
		return Objects.equals(inventoryDate, other.inventoryDate) && Objects.equals(roomTypeId, other.roomTypeId);
	}
	
	

}
