package com.example.thestar1.order.entity;
import com.example.thestar1.stayrecord.entity.StayRecordVO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ROOM_ORDER_LIST")
public class OrderListVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ORDER_LIST_ID", updatable = false)
	private Integer orderListId;

	@Column(name = "ROOM_TYPE_ID")
	private Integer roomTypeId;

	@Column(name = "QUANTITY")
	private Integer quantity;

	@Column(name = "ROOM_PRICE")
	private Integer roomPrice;

	@Column(name = "SUBTOTAL")
	private Integer subtotal;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORDER_ID")
	private OrderVO ordervo;


	@OneToMany(mappedBy = "orderListvo", fetch = FetchType.LAZY)
	private List<StayRecordVO> stayRecord;

	public OrderListVO() {
		super();
	}

	public OrderListVO(Integer orderListId, Integer roomTypeId, Integer quantity, Integer roomPrice,
			Integer subtotal, OrderVO ordervo) {
		super();
		this.orderListId = orderListId;
		this.roomTypeId = roomTypeId;
		this.quantity = quantity;
		this.roomPrice = roomPrice;
		this.subtotal = subtotal;
		this.ordervo = ordervo;
	}

	public List<StayRecordVO> getStayRecord() {
		return stayRecord;
	}

	public void setStayRecord(List<StayRecordVO> stayRecord) {
		this.stayRecord = stayRecord;
	}

	public Integer getOrderListId() {
		return orderListId;
	}

	public void setOrderListId(Integer orderListId) {
		this.orderListId = orderListId;
	}

	public Integer getRoomTypeId() {
		return roomTypeId;
	}

	public void setRoomTypeId(Integer roomTypeId) {
		this.roomTypeId = roomTypeId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getRoomPrice() {
		return roomPrice;
	}

	public void setRoomPrice(Integer roomPrice) {
		this.roomPrice = roomPrice;
	}

	public Integer getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(Integer subtotal) {
		this.subtotal = subtotal;
	}

	public OrderVO getOrdervo() {
		return ordervo;
	}

	public void setOrdervo(OrderVO ordervo) {
		this.ordervo = ordervo;
	}

}
