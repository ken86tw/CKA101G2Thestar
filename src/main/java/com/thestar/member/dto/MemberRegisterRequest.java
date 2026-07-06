package com.thestar.member.dto;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class MemberRegisterRequest {

    private String memberName;
    private String memberEmail;
    private String memberPassword;
    private String confirmPassword;
    private String memberPhone;
    private String memberAddress;
    private LocalDate memberBirthday;
    private Byte memberGender;
    private MultipartFile memberPicture;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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

    public MultipartFile getMemberPicture() {
        return memberPicture;
    }

    public void setMemberPicture(MultipartFile memberPicture) {
        this.memberPicture = memberPicture;
    }
}
