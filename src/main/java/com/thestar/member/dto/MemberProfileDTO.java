package com.thestar.member.dto;

import com.thestar.member.entity.MemberVO;

import java.time.LocalDate;

public class MemberProfileDTO {

    private Integer memberId;
    private String memberName;
    private String memberEmail;
    private String memberPhone;
    private String memberAddress;
    private LocalDate memberBirthday;
    private Byte memberGender;
    private Byte memberStatus;

    public static MemberProfileDTO from(MemberVO member) {
        MemberProfileDTO dto = new MemberProfileDTO();
        dto.memberId = member.getMemberId();
        dto.memberName = member.getMemberName();
        dto.memberEmail = member.getMemberEmail();
        dto.memberPhone = member.getMemberPhone();
        dto.memberAddress = member.getMemberAddress();
        dto.memberBirthday = member.getMemberBirthday();
        dto.memberGender = member.getMemberGender();
        dto.memberStatus = member.getMemberStatus();
        return dto;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public String getMemberAddress() {
        return memberAddress;
    }

    public LocalDate getMemberBirthday() {
        return memberBirthday;
    }

    public Byte getMemberGender() {
        return memberGender;
    }

    public Byte getMemberStatus() {
        return memberStatus;
    }
}