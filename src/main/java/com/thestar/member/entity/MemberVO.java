package com.thestar.member.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

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

    @Column(name = "MEMBER_PASSWORD", nullable = false, length = 255)
    private String memberPassword;

    @Column(name = "MEMBER_PHONE", nullable = false, length = 20)
    private String memberPhone;

    @Column(name = "MEMBER_ADDRESS", nullable = false, length = 100)
    private String memberAddress;

    @Column(name = "MEMBER_BIRTHDAY")
    private LocalDate memberBirthday; // 欄位為 DATE，建議用 LocalDate

    @Column(name = "MEMBER_GENDER", columnDefinition = "TINYINT")
    private Integer memberGender; // 0:FEMALE, 1:MALE, 2:UNKNOWN

    @Lob
    @Column(name = "MEMBER_PICTURE", columnDefinition = "LONGBLOB")
    private byte[] memberPicture; // 對應 LONGBLOB 欄位

    @Column(name = "MEMBER_STATUS", nullable = false, columnDefinition = "TINYINT")
    private Integer memberStatus = 0; // 預設 0:NOT ENABLED, 1:ENABLED, 2:DISENABLED

    @Column(name = "CREATED_TIME", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "RESET_TOKEN", length = 255)
    private String resetToken;

    @Column(name = "RESET_EXPIRE_TIME")
    private LocalDateTime resetExpireTime;

    // --- 在持久化之前自動設定建立時間 ---
    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
    }

    // --- Getters and Setters ---

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

    public Integer getMemberGender() {
        return memberGender;
    }

    public void setMemberGender(Integer memberGender) {
        this.memberGender = memberGender;
    }

    public byte[] getMemberPicture() {
        return memberPicture;
    }

    public void setMemberPicture(byte[] memberPicture) {
        this.memberPicture = memberPicture;
    }

    public Integer getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(Integer memberStatus) {
        this.memberStatus = memberStatus;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
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

    // --- 修改後的 toString() ---
    @Override
    public String toString() {
        return "MemberVO {" +
                "memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", memberEmail='" + memberEmail + '\'' +
                ", memberStatus=" + memberStatus +
                '}';
    }
}