package com.example.thestar1.refund.entity;
import com.example.thestar1.order.entity.OrderVO;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "REFUND_LIST")
public class RefundListVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "REFUND_ID", updatable = false)
	private Integer refundId;
	
	@Column(name ="AMOUNT")
	private Integer amount;
	
	@Column(name = "REFUND_STATUS")
	private Byte refundStatus;
	
	@Column(name ="ECPAY_REFUND_NO")
	private String ecpayRefundNo;
	
	@CreationTimestamp
	@Column(name ="CREATED_TIME")
	private LocalDateTime createdTime;
	
	@Column(name ="REFUND_TIME")
	private LocalDateTime refundTime;
	
	@Column(name ="REASON")
	private String reason;
	
	@Column(name ="EMPLOYEE_ID")
	private Integer employeeId;
	
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORDER_ID")
	private OrderVO ordervo;

	public RefundListVO() {
		super();
	}

	public Integer getRefundId() {
		return refundId;
	}

	public void setRefundId(Integer refundId) {
		this.refundId = refundId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Byte getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(Byte refundStatus) {
		this.refundStatus = refundStatus;
	}

	public String getEcpayRefundNo() {
		return ecpayRefundNo;
	}

	public void setEcpayRefundNo(String ecpayRefundNo) {
		this.ecpayRefundNo = ecpayRefundNo;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public LocalDateTime getRefundTime() {
		return refundTime;
	}

	public void setRefundTime(LocalDateTime refundTime) {
		this.refundTime = refundTime;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public OrderVO getOrdervo() {
		return ordervo;
	}

	public void setOrdervo(OrderVO ordervo) {
		this.ordervo = ordervo;
	}
	
	
	
}
