package com.thestar.member.service;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    /**
     * 透過 ID 查詢會員（提供給你前面評論頁面使用的）
     */
    public MemberVO getMemberById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("找不到該會員，ID: " + memberId));
    }

    /**
     * 處理登入邏輯（透過 Email 驗證）
     */

}