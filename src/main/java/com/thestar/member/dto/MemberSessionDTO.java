package com.thestar.member.dto;

import com.thestar.member.entity.MemberVO;

public class MemberSessionDTO {

    private boolean loggedIn;
    private Integer memberId;
    private String memberName;
    private String memberEmail;

    public static MemberSessionDTO guest() {
        MemberSessionDTO dto = new MemberSessionDTO();
        dto.loggedIn = false;
        return dto;
    }

    public static MemberSessionDTO from(MemberVO member) {
        MemberSessionDTO dto = new MemberSessionDTO();
        dto.loggedIn = true;
        dto.memberId = member.getMemberId();
        dto.memberName = member.getMemberName();
        dto.memberEmail = member.getMemberEmail();
        return dto;
    }

    public boolean isLoggedIn() {
        return loggedIn;
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
}