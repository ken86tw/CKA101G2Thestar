package com.example.thestar1.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ROOM_INVENTORY")
public class RoomInventoryVO {
	
	@EmbeddedId
	private RoomInventoryId id;
	
	@Column(name = "TOTAL_AMOUNT")
	private Integer totalCount;
	
	@Column(name = "BOOKED_AMOUNT")
	private Integer bookedCount;

	public RoomInventoryVO() {
		super();
	}

	public RoomInventoryId getId() {
		return id;
	}

	public void setId(RoomInventoryId id) {
		this.id = id;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getBookedCount() {
		return bookedCount;
	}

	public void setBookedCount(Integer bookedCount) {
		this.bookedCount = bookedCount;
	}
	
	
}
