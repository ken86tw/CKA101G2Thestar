package com.thestar.feedback.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.feedback.entity.FeedbackVO;
import com.thestar.feedback.repository.FeedbackRepository;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberService;
import com.thestar.member.service.MemberVerificationMailService;

@Service
public class FeedbackService {

	@Autowired
	private FeedbackRepository repository;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberVerificationMailService mailService;

	// 建立回報，使用者填寫
	public FeedbackVO createFeedback(FeedbackVO feedback) {
		// 1. 檢查主旨
	    if (feedback.getSubject() == null || feedback.getSubject().trim().isEmpty()) {
	        throw new IllegalArgumentException("主旨不得為空");
	    }

	    // 2. 檢查內容
	    if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
	        throw new IllegalArgumentException("內容不得為空");
	    }

	    // 3. 【關鍵修正】檢查 memberId 是否存在
	    if (feedback.getMemberId() == null) {
	        throw new IllegalArgumentException("會員ID不得為空");
	    }
	    
	    // 透過你的 memberService 查詢該會員是否存在
	    MemberVO member = memberService.getMemberById(feedback.getMemberId());
	    if (member == null) {
	        // 如果查不到，這裡直接中斷，不會送到資料庫去報錯
	        throw new IllegalArgumentException("找不到此會員，無法建立回報");
	    }

	    // 4. 設定初始值
	    feedback.setCreatedAt(LocalDateTime.now());
	    feedback.setRepliedAt(null);
	    feedback.setTicketStatus((byte) 0); // 確保狀態初始為 0

	    return repository.save(feedback);
	}

	// 檢查問題狀態
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

	// 查詢所有紀錄
	public List<FeedbackVO> getAllFeedback() {
		// 1. 先從資料庫撈出所有回報
		List<FeedbackVO> list = repository.findAll();

		// 2. 遍歷清單，為每一筆回報補上會員姓名
		for (FeedbackVO feedback : list) {
			if (feedback.getMemberId() != null) {
				// 透過 memberService 根據 ID 查詢會員
				MemberVO member = memberService.getMemberById(feedback.getMemberId());
				if (member != null) {
					// 將查到的姓名塞入 @Transient 欄位
					feedback.setMemberName(member.getMemberName());
				}
			}
		}

		return list;
	}

	// 客服回覆
	public boolean sendMailToMember(Integer ticketId, String mail, String message) {
		String sub = "The Star Hotel 問題回報回覆 (案件編號: " + ticketId + ")";

		try {
			// 嘗試寄信
			mailService.sendMail(mail, sub, message);

			// 只有在 mailService 確定沒有拋出異常時，才回傳 true
			return true;

		} catch (Exception e) {
			// 1. 記錄詳細日誌（這對 Debug 很重要）
			System.err.println("寄信失敗，案件編號: " + ticketId + "，錯誤訊息: " + e.getMessage());

			// 2. 拋出一個自定義異常，或是回傳 false
			// 建議：如果是為了讓 Controller 處理錯誤，回傳 false 是合理的
			return false;
		}
	}
}
