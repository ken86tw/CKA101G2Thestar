package com.thestar.feedback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.thestar.feedback.entity.FeedbackVO;
import com.thestar.feedback.service.FeedbackService;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

	@Autowired
	private FeedbackService service;

	// 會員回報畫面
	@GetMapping("/report")
	public String showReportForm() {
		return "user/feedback/report";
	}

	// 員工管理、回覆畫面
	@GetMapping("/manage")
	public String showFeedbackReply() {
		return "/admin/feedback/reply";
	}

	// @ResponseBody 才能繼續回傳 JSON
	// 接收問題回報
	@PostMapping("/add")
	@ResponseBody
	public FeedbackVO add(@RequestBody FeedbackVO feedback) {
		return service.createFeedback(feedback);
	}

	// 接收來自前端網頁report的請求」，並呼叫服務層（Service）來完成問題回報的資料庫更新
	@PostMapping("/reply")
	@ResponseBody
	public FeedbackVO reply(@RequestParam Integer ticketId, @RequestParam String replyContent,
			@RequestParam Integer employeeId) {
		return service.replyFeedback(ticketId, replyContent, employeeId);
	}

	// 查詢所有問題回報紀錄
	@GetMapping("/all")
	@ResponseBody
	public List<FeedbackVO> findAll() {
		return service.getAllFeedback();
	}

	@PostMapping("/send")
	@ResponseBody
	public String send(@RequestParam Integer ticketId,
			@RequestParam String email, @RequestParam String message) {

		// 呼叫 Service 層進行寄信
		boolean isSent = service.sendMailToMember(ticketId, email, message);

		if (isSent) {
			return "success"; // 回傳 JSON 給前端處理
		} else {
			return "failure";
		}
	}
}