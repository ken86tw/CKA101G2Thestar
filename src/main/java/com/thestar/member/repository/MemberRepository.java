package com.thestar.member.repository;

import com.thestar.member.entity.MemberVO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberVO, Integer> {

    /**
     * 透過會員信箱 (Email) 查詢會員資料
     * 登入時會用到此方法，來確認該信箱是否存在、密碼是否正確
     *
     * @param memberEmail 登入時輸入的信箱
     * @return 回傳 Optional 包裹的 MemberVO，方便後續進行 null 的安全檢查
     */
    Optional<MemberVO> findByMemberEmail(String memberEmail);

    /**
     * 透過會員信箱查詢會員資料（忽略大小寫）
     *
     * @param memberEmail 會員信箱
     * @return Optional 包裹的 MemberVO
     */
    Optional<MemberVO> findByMemberEmailIgnoreCase(String memberEmail);

    /**
     * 判斷會員信箱是否已存在（忽略大小寫）
     *
     * @param memberEmail 會員信箱
     * @return 存在回傳 true，否則 false
     */
    boolean existsByMemberEmailIgnoreCase(String memberEmail);

    /**
     * 透過驗證 Token 查詢會員資料
     * 用於信箱驗證流程
     *
     * @param verifyToken 驗證 Token
     * @return Optional 包裹的 MemberVO
     */
    Optional<MemberVO> findByVerifyToken(String verifyToken);

    /**
     * 查詢指定生日月份且帳號啟用中的會員
     * 用於生日優惠、生日通知等功能
     *
     * @param birthMonth 生日月份
     * @return 符合條件的會員列表
     */
    @Query("""
            SELECT member
            FROM MemberVO member
            WHERE member.memberStatus = 1
              AND FUNCTION('MONTH', member.memberBirthday) = :birthMonth
            """)
    List<MemberVO> findEnabledMembersByBirthMonth(
            @Param("birthMonth") Integer birthMonth
    );

    /**
     * 查詢指定狀態的會員，供後台發券時選擇。
     */
    List<MemberVO> findByMemberStatusOrderByMemberIdAsc(Byte memberStatus);

}