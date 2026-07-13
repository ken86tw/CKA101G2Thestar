package com.thestar.member.dto;

public class MemberProfileUpdateRequest {

    private String memberName;
    private String memberPhone;
    private String memberAddress;
    private Byte memberGender;

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
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

    public Byte getMemberGender() {
        return memberGender;
    }

    public void setMemberGender(Byte memberGender) {
        this.memberGender = memberGender;
    }
}