package com.thestar.stayrecord.entity;
import com.thestar.order.entity.OrderListVO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

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

	@JsonIgnore
	@Lob
	@Column(name = "STAY_CUSTOMER_PHOTO")
	private byte[] stayCustomerPhoto;

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

	public byte[] getStayCustomerPhoto() {
		return stayCustomerPhoto;
	}

	public void setStayCustomerPhoto(byte[] stayCustomerPhoto) {
		this.stayCustomerPhoto = stayCustomerPhoto;
	}





}
