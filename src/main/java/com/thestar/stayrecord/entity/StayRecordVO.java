package com.example.thestar1.stayrecord.entity;
import com.example.thestar1.order.entity.OrderListVO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "STAY_RECORD")
public class StayRecordVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY )
	@Column(name = "STAY_ID",updatable = false)
	private Integer stayId;
	
	@Column(name = "ROOM_ID")
	private Integer roomId;

	@Column(name = "STAY_CUSTOMER")
	private String stayCustomer;

	@Column(name = "CHECK_IN_EMPLOYEE_ID")
	private Integer checkInEmployeeId;
	
	@Column(name = "CHECK_OUT_EMPLOYEE_ID")
	private Integer checkOutEmployeeId;
	
	@CreationTimestamp
	@Column(name= "CHECK_IN_TIME")
	private LocalDateTime checkInTime;
	
	@Column(name= "CHECK_OUT_TIME")
	private LocalDateTime checkOutTime;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORDER_LIST_ID")
	private OrderListVO orderListvo;

	public StayRecordVO() {
		super();
	}

	public Integer getStayId() {
		return stayId;
	}

	public void setStayId(Integer stayId) {
		this.stayId = stayId;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Integer getCheckInEmployeeId() {
		return checkInEmployeeId;
	}

	public void setCheckInEmployeeId(Integer checkInEmployeeId) {
		this.checkInEmployeeId = checkInEmployeeId;
	}

	public Integer getCheckOutEmployeeId() {
		return checkOutEmployeeId;
	}

	public void setCheckOutEmployeeId(Integer checkOutEmployeeId) {
		this.checkOutEmployeeId = checkOutEmployeeId;
	}

	public LocalDateTime getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(LocalDateTime checkInTime) {
		this.checkInTime = checkInTime;
	}

	public LocalDateTime getCheckOutTime() {
		return checkOutTime;
	}

	public void setCheckOutTime(LocalDateTime checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	public OrderListVO getOrderListvo() {
		return orderListvo;
	}

	public void setOrderListvo(OrderListVO orderListvo) {
		this.orderListvo = orderListvo;
	}

	public String getStayCustomer() {
		return stayCustomer;
	}

	public void setStayCustomer(String stayCustomer) {
		this.stayCustomer = stayCustomer;
	}




}
