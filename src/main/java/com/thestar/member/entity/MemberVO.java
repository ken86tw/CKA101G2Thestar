package com.thestar.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBERS")
public class MemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Integer memberId;

    @Column(name = "MEMBER_NAME", nullable = false, length = 50)
    private String memberName;

    @Column(name = "MEMBER_EMAIL", nullable = false, unique = true, length = 100)
    private String memberEmail;

    @JsonIgnore
    @Column(name = "MEMBER_PASSWORD", nullable = false, length = 255)
    private String memberPassword;

    @Column(name = "MEMBER_PHONE", nullable = false, length = 20)
    private String memberPhone;

    @Column(name = "MEMBER_ADDRESS", nullable = false, length = 100)
    private String memberAddress;

    @Column(name = "MEMBER_BIRTHDAY")
    private LocalDate memberBirthday;

    @Column(name = "MEMBER_GENDER")
    private Byte memberGender;

    @JsonIgnore
    @Lob
    @Column(name = "MEMBER_PICTURE", columnDefinition = "LONGBLOB")
    private byte[] memberPicture;

    @Column(name = "MEMBER_STATUS", nullable = false)
    private Byte memberStatus = 0;

    @Column(name = "CREATED_TIME", insertable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "UPDATED_TIME", insertable = false, updatable = false)
    private LocalDateTime updatedTime;

    @JsonIgnore
    @Column(name = "RESET_TOKEN", length = 255)
    private String resetToken;

    @JsonIgnore
    @Column(name = "RESET_EXPIRE_TIME")
    private LocalDateTime resetExpireTime;

    @JsonIgnore
    @Column(name = "VERIFY_TOKEN", length = 255)
    private String verifyToken;

    @JsonIgnore
    @Column(name = "VERIFY_EXPIRE_TIME")
    private LocalDateTime verifyExpireTime;

    public MemberVO() {
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getMemberPassword() {
        return memberPassword;
    }

    public void setMemberPassword(String memberPassword) {
        this.memberPassword = memberPassword;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public void setMemberPhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public String getMemberAddress() {
        return memberAddress;
    }

    public void setMemberAddress(String memberAddress) {
        this.memberAddress = memberAddress;
    }

    public LocalDate getMemberBirthday() {
        return memberBirthday;
    }

    public void setMemberBirthday(LocalDate memberBirthday) {
        this.memberBirthday = memberBirthday;
    }

    public Byte getMemberGender() {
        return memberGender;
    }

    public void setMemberGender(Byte memberGender) {
        this.memberGender = memberGender;
    }

    public byte[] getMemberPicture() {
        return memberPicture;
    }

    public void setMemberPicture(byte[] memberPicture) {
        this.memberPicture = memberPicture;
    }

    public Byte getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(Byte memberStatus) {
        this.memberStatus = memberStatus;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetExpireTime() {
        return resetExpireTime;
    }

    public void setResetExpireTime(LocalDateTime resetExpireTime) {
        this.resetExpireTime = resetExpireTime;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }

    public LocalDateTime getVerifyExpireTime() {
        return verifyExpireTime;
    }

    public void setVerifyExpireTime(LocalDateTime verifyExpireTime) {
        this.verifyExpireTime = verifyExpireTime;
    }

    @Override
    public String toString() {
        return "MemberVO{" +
                "memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", memberEmail='" + memberEmail + '\'' +
                ", memberPhone='" + memberPhone + '\'' +
                ", memberStatus=" + memberStatus +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}