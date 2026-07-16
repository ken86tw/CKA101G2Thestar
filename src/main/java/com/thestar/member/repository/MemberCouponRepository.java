package com.thestar.member.repository;

import com.thestar.member.entity.MemberCouponVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponRepository
        extends JpaRepository<MemberCouponVO, Integer> {

    @EntityGraph(attributePaths = "coupon")
    List<MemberCouponVO>
    findByMemberIdOrderByClaimedTimeDesc(
            Integer memberId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "coupon")
    @Query("""
           SELECT mc
           FROM MemberCouponVO mc
           WHERE mc.memberCouponId = :memberCouponId
           """)
    Optional<MemberCouponVO> findByIdForUpdate(
            @Param("memberCouponId") Integer memberCouponId
    );
    
    boolean existsByMemberIdAndCoupon_CouponIdAndIssuePeriod(
            Integer memberId,
            Integer couponId,
            String issuePeriod
    );
    
    @EntityGraph(attributePaths = "coupon")
    List<MemberCouponVO>
    findByUsedStatusAndUsageEndTimeBetween(
            Byte usedStatus,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @EntityGraph(attributePaths = "coupon")
    List<MemberCouponVO> findAllByOrderByClaimedTimeDesc();

    boolean existsByCoupon_CouponId(Integer couponId);
}