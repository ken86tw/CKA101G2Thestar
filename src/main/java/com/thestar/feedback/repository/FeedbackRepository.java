package com.thestar.feedback.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thestar.feedback.entity.FeedbackVO;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackVO, Integer> {
	List<FeedbackVO> findByTicketStatus(Integer ticketStatus);

	List<FeedbackVO> findByMemberId(Integer memberId);

}
