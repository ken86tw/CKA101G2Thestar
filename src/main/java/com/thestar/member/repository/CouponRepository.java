package com.thestar.member.repository;

import com.thestar.member.entity.CouponVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository
        extends JpaRepository<CouponVO, Integer> {

    Optional<CouponVO> findByCouponCode(
            String couponCode
    );

    Optional<CouponVO> findByCouponCodeIgnoreCase(
            String couponCode
    );

    Optional<CouponVO> findByCouponCodeAndIssueStatus(
            String couponCode,
            Byte issueStatus
    );
}