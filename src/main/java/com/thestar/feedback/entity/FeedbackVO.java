package com.thestar.feedback.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FEEDBACK")
public class FeedbackVO {

	// 問題回報ID
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TICKET_ID")
	private Integer ticketId;

	@Column(name = "MEMBER_ID", nullable = false)
	private Integer memberId;

	@Column(name = "EMPLOYEE_ID")
	private Integer employeeId;

	@Column(name = "EMAIL", nullable = false)
	private String email;

	// 問題主旨
	@Column(name = "SUBJECT", nullable = false)
	private String subject;
	
	//問題內容
	@Column(name = "CONTENT", nullable = false)
	private String content;

	// 員工回覆內容
	@Column(name = "REPLY_CONTENT")
	private String replyContent;

	@Column(name = "TICKET_STATUS", nullable = false)
	private Byte ticketStatus = 0;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "REPLIED_AT")
	private LocalDateTime repliedAt;

	public FeedbackVO() {
	}

	public Integer getTicketId() {
		return ticketId;
	}

	public void setTicketId(Integer ticketId) {
		this.ticketId = ticketId;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getReplyContent() {
		return replyContent;
	}

	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}

	public Byte getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(Byte ticketStatus) {
		this.ticketStatus = ticketStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getRepliedAt() {
		return repliedAt;
	}

	public void setRepliedAt(LocalDateTime repliedAt) {
		this.repliedAt = repliedAt;
	}

}
