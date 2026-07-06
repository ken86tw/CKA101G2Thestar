package com.thestar.member.repository;

import com.thestar.member.entity.MemberVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberVO, Integer> {

	/**
	 * 透過會員信箱 (Email) 查詢會員資料 登入時會用到此方法，來確認該信箱是否存在、密碼是否正確 * @param memberEmail
	 * 登入時輸入的信箱
	 * 
	 * @return 回傳 Optional 包裹的 MemberVO，方便後續進行 null 的安全檢查
	 */
	Optional<MemberVO> findByMemberEmail(String memberEmail);

	Optional<MemberVO> findByMemberEmailIgnoreCase(String memberEmail);

	boolean existsByMemberEmailIgnoreCase(String memberEmail);

	Optional<MemberVO> findByVerifyToken(String verifyToken);
}
