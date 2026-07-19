package com.thestar.feedback.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.feedback.entity.FeedbackVO;
import com.thestar.feedback.repository.FeedbackRepository;
import com.thestar.member.service.MemberVerificationMailService;

@Service
public class FeedbackService {

	@Autowired
	private FeedbackRepository repository;

	@Autowired
	private MemberVerificationMailService mailService;

	public FeedbackVO createFeedback(FeedbackVO feedback) {
		return repository.save(feedback);
	}

	public FeedbackVO replyFeedback(Integer ticketId, String replyContent, Integer employeeId) {
		FeedbackVO feedback = repository.findById(ticketId).orElseThrow();
		
		if (feedback.getTicketStatus() == 1) {
	        throw new IllegalStateException("此案件已回覆，無法重複操作！");
	    }
		
		feedback.setReplyContent(replyContent);
		feedback.setEmployeeId(employeeId);
		feedback.setTicketStatus((byte) 1);
		feedback.setRepliedAt(LocalDateTime.now());
		return repository.save(feedback);
	}

	public List<FeedbackVO> getAllFeedback() {
		return repository.findAll();
	}

	public boolean sendMailToMember(Integer ticketId, String mail, String message) {
		String sub = "The Star Hotel 問題回報回覆 (案件編號: " + ticketId + ")";

		// 呼叫現有的 mailService
		try {
			mailService.sendMail(mail, sub, message);
			return true;
		} catch (Exception e) {
			// 記錄錯誤日誌
			e.printStackTrace();
			return false;
		}
	}
}
