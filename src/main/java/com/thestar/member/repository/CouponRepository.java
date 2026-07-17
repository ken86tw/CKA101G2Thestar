package com.thestar.member.repository;

import com.thestar.member.entity.CouponVO;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponVO c WHERE c.couponId = :couponId")
    Optional<CouponVO> findByIdForUpdate(
            @Param("couponId") Integer couponId
    );
}