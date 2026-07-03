package com.example.thestar1.member.entity;

// ⚠️ 暫時 stub：只為讓 Controller 從 session 取 loginMember 能編譯。
// 會員模組是隊友的，這裡刻意不是 @Entity（避免 ddl-validate 比對 schema）。
// 等隊友真正的 Member 進 repo 後，刪掉這個檔案。
public class MemberVO {

    private Integer memberId;

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }
}