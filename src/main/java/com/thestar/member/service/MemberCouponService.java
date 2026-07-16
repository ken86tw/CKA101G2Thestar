package com.thestar.member.service;

import com.thestar.member.dto.CouponAdminForm;
import com.thestar.member.dto.MemberCouponDTO;
import com.thestar.member.dto.MemberCouponAdminDTO;
import com.thestar.member.entity.CouponVO;
import com.thestar.member.entity.MemberCouponVO;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.repository.MemberRepository;
import com.thestar.member.repository.CouponRepository;
import com.thestar.member.repository.MemberCouponRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.time.YearMonth;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MemberCouponService {

	private static final byte NOT_USED = 0;
	private static final byte USED = 1;
	private static final byte ISSUE_ACTIVE = 1;

	private static final String NEW_MEMBER_CODE = "NEW_MEMBER";

	private static final String ISSUE_PERIOD_ONCE = "ONCE";

	private static final String BIRTHDAY_CODE = "BIRTHDAY";

	private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

	private final MemberCouponRepository memberCouponRepository;
	private final CouponRepository couponRepository;
	private final MemberRepository memberRepository;
	private final MemberNotifyService memberNotifyService;

	public MemberCouponService(MemberCouponRepository memberCouponRepository, CouponRepository couponRepository,
			MemberRepository memberRepository, MemberNotifyService memberNotifyService) {
		this.memberCouponRepository = memberCouponRepository;
		this.couponRepository = couponRepository;
		this.memberRepository = memberRepository;
		this.memberNotifyService = memberNotifyService;
	}

	@Transactional(readOnly = true)
	public List<MemberCouponDTO> getMemberCoupons(Integer memberId) {
		if (memberId == null) {
			throw new IllegalArgumentException("會員編號不可為空");
		}

		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		return memberCouponRepository.findByMemberIdOrderByClaimedTimeDesc(memberId).stream()
				.map(memberCoupon -> toDTO(memberCoupon, now)).toList();
	}

	@Transactional
	public boolean issueNewMemberCoupon( // 新會員券
			Integer memberId) {
		if (memberId == null) {
			throw new IllegalArgumentException("會員編號不可為空");
		}

		CouponVO coupon = couponRepository.findByCouponCodeAndIssueStatus(NEW_MEMBER_CODE, ISSUE_ACTIVE).orElse(null);

		/*
		 * 找不到優惠券，或優惠券已暫停發放。 不影響會員信箱驗證。
		 */
		if (coupon == null) {
			return false;
		}

		boolean alreadyIssued = memberCouponRepository.existsByMemberIdAndCoupon_CouponIdAndIssuePeriod(memberId,
				coupon.getCouponId(), ISSUE_PERIOD_ONCE);
		/*
		 * 已經發放過，不重複新增。
		 */
		if (alreadyIssued) {
			return false;
		}

		Integer remainingQuantity = coupon.getRemainingQuantity();

		/*
		 * NULL 代表不限量。 數量為 0 代表已經發完。
		 */
		if (remainingQuantity != null && remainingQuantity <= 0) {

			return false;
		}

		Integer validDays = coupon.getDefaultValidDays();

		/*
		 * 新會員券必須設定有效天數。
		 */
		if (validDays == null || validDays <= 0) {
			return false;
		}

		LocalDateTime claimedTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		LocalDateTime usageStartTime = claimedTime;

		/*
		 * 發放當天算第 1 天。
		 *
		 * 例如 30 天： 發放日 + 29 天的 23:59:59 到期。
		 */
		LocalDateTime usageEndTime = claimedTime.toLocalDate().plusDays(validDays - 1L).atTime(23, 59, 59);

		MemberCouponVO memberCoupon = new MemberCouponVO();

		memberCoupon.setMemberId(memberId);
		memberCoupon.setCoupon(coupon);
		memberCoupon.setIssuePeriod(ISSUE_PERIOD_ONCE);
		memberCoupon.setUsedStatus(NOT_USED);
		memberCoupon.setClaimedTime(claimedTime);
		memberCoupon.setUsageStartTime(usageStartTime);
		memberCoupon.setUsageEndTime(usageEndTime);
		memberCoupon.setUsedTime(null);

		/*
		 * 有設定數量時才扣除。 NULL 代表不限量，不需要扣除。
		 */
		memberCouponRepository.save(memberCoupon);

		String notificationContent = "您的「" + coupon.getCouponName() + "」已發送至會員帳戶，" + "請於 " + usageEndTime.toLocalDate() + " 前使用。";

		memberNotifyService.createNotification(memberId, notificationContent);

		if (remainingQuantity != null) {

			coupon.setRemainingQuantity(remainingQuantity - 1);

			couponRepository.save(coupon);
		}

		return true;
	}

	@Transactional
	public int issueCurrentMonthBirthdayCoupons() { // 發放每月生日券

		YearMonth currentMonth = YearMonth.now();
		Integer birthMonth = currentMonth.getMonthValue();
		String issuePeriod = currentMonth.toString();

		CouponVO birthdayCoupon = couponRepository.findByCouponCodeAndIssueStatus(BIRTHDAY_CODE, ISSUE_ACTIVE)
				.orElse(null);

		/*
		 * 找不到生日券，或生日券已暫停發放。
		 */
		if (birthdayCoupon == null) {
			return 0;
		}

		List<MemberVO> birthdayMembers = memberRepository.findEnabledMembersByBirthMonth(birthMonth);

		/*
		 * 本月沒有啟用中的壽星會員。
		 */
		if (birthdayMembers.isEmpty()) {
			return 0;
		}

		LocalDateTime claimedTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		/*
		 * 如果在每月 1 日凌晨發放， 使用時間從當月 1 日開始。
		 *
		 * 如果在月中補發， 使用時間從實際發放時間開始。
		 */
		LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();

		LocalDateTime usageStartTime = claimedTime.isAfter(monthStart) ? claimedTime : monthStart;

		LocalDateTime usageEndTime = currentMonth.atEndOfMonth().atTime(23, 59, 59);

		Integer remainingQuantity = birthdayCoupon.getRemainingQuantity();

		int issuedCount = 0;

		for (MemberVO member : birthdayMembers) {

			/*
			 * 有數量限制，而且已經發完。
			 */
			if (remainingQuantity != null && remainingQuantity <= 0) {

				break;
			}

			boolean alreadyIssued = memberCouponRepository.existsByMemberIdAndCoupon_CouponIdAndIssuePeriod(
					member.getMemberId(), birthdayCoupon.getCouponId(), issuePeriod);
			/*
			 * 同一會員本月已經拿過生日券。
			 */
			if (alreadyIssued) {
				continue;
			}

			MemberCouponVO memberCoupon = new MemberCouponVO();

			memberCoupon.setMemberId(member.getMemberId());

			memberCoupon.setCoupon(birthdayCoupon);

			memberCoupon.setIssuePeriod(issuePeriod);

			memberCoupon.setUsedStatus(NOT_USED);

			memberCoupon.setClaimedTime(claimedTime);

			memberCoupon.setUsageStartTime(usageStartTime);

			memberCoupon.setUsageEndTime(usageEndTime);

			memberCoupon.setUsedTime(null);

			memberCouponRepository.save(memberCoupon);

			String notificationContent =
			        "您的「"
			        + birthdayCoupon.getCouponName()
			        + "」已發送至會員帳戶，"
			        + "請於 "
			        + usageEndTime.toLocalDate()
			        + " 前使用。";

			memberNotifyService.createNotification(
			        member.getMemberId(),
			        notificationContent
			);

			issuedCount++;

			/*
			 * NULL 代表不限量。
			 */
			if (remainingQuantity != null) {
				remainingQuantity--;
			}
		}
		/*
		 * 有設定限量時，更新剩餘數量。
		 */
		if (birthdayCoupon.getRemainingQuantity() != null) {

			birthdayCoupon.setRemainingQuantity(remainingQuantity);
			couponRepository.save(birthdayCoupon);
		}
		return issuedCount;
	}

	@Transactional
	public boolean issueBirthdayCouponForMember( // 驗證成功時，檢查這一位會員
			Integer memberId) {
		if (memberId == null) {
			return false;
		}

		MemberVO member = memberRepository.findById(memberId).orElse(null);

		/*
		 * 找不到會員。
		 */
		if (member == null) {
			return false;
		}

		/*
		 * 只發給已啟用會員。
		 */
		if (member.getMemberStatus() == null || member.getMemberStatus() != 1) {
			return false;
		}

		/*
		 * 沒有填生日，不能判斷是否為當月壽星。
		 */
		if (member.getMemberBirthday() == null) {
			return false;
		}

		YearMonth currentMonth = YearMonth.now();

		int currentMonthValue = currentMonth.getMonthValue();

		int birthdayMonth = member.getMemberBirthday().getMonthValue();

		/*
		 * 不是當月壽星，不發放。
		 */
		if (birthdayMonth != currentMonthValue) {
			return false;
		}

		CouponVO birthdayCoupon = couponRepository.findByCouponCodeAndIssueStatus(BIRTHDAY_CODE, ISSUE_ACTIVE)
				.orElse(null);

		/*
		 * 找不到生日券，或生日券已暫停發放。
		 */
		if (birthdayCoupon == null) {
			return false;
		}

		String issuePeriod = currentMonth.toString();

		boolean alreadyIssued = memberCouponRepository.existsByMemberIdAndCoupon_CouponIdAndIssuePeriod(memberId,
				birthdayCoupon.getCouponId(), issuePeriod);
		/*
		 * 會員本月已經領過生日券。
		 */
		if (alreadyIssued) {
			return false;
		}

		Integer remainingQuantity = birthdayCoupon.getRemainingQuantity();

		/*
		 * NULL 代表不限量。 0 代表已發完。
		 */
		if (remainingQuantity != null && remainingQuantity <= 0) {
			return false;
		}

		LocalDateTime claimedTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		LocalDateTime usageStartTime = claimedTime;

		LocalDateTime usageEndTime = currentMonth.atEndOfMonth().atTime(23, 59, 59);

		MemberCouponVO memberCoupon = new MemberCouponVO();

		memberCoupon.setMemberId(memberId);
		memberCoupon.setCoupon(birthdayCoupon);
		memberCoupon.setIssuePeriod(issuePeriod);
		memberCoupon.setUsedStatus(NOT_USED);
		memberCoupon.setClaimedTime(claimedTime);
		memberCoupon.setUsageStartTime(usageStartTime);
		memberCoupon.setUsageEndTime(usageEndTime);
		memberCoupon.setUsedTime(null);

		memberCouponRepository.save(memberCoupon);

		String notificationContent =
		        "您的「"
		        + birthdayCoupon.getCouponName()
		        + "」已發送至會員帳戶，"
		        + "請於 "
		        + usageEndTime.toLocalDate()
		        + " 前使用。";

		memberNotifyService.createNotification(
		        memberId,
		        notificationContent
		);

		/*
		 * 有設定限量時才扣除數量。
		 */
		if (remainingQuantity != null) {

		    birthdayCoupon.setRemainingQuantity(
		            remainingQuantity - 1
		    );

		    couponRepository.save(birthdayCoupon);
		}
		return true;
	}
	
	@Transactional
	public int useCouponForRoomOrder(
	        Integer memberId,
	        Integer memberCouponId,
	        int totalAmount
	) {
	    if (memberId == null || memberCouponId == null) {
	        throw new IllegalArgumentException("會員與優惠券編號不可為空");
	    }

	    if (totalAmount <= 0) {
	        throw new IllegalArgumentException("訂單金額不正確");
	    }

	    MemberCouponVO memberCoupon =
	            memberCouponRepository
	                    .findByIdForUpdate(memberCouponId)
	                    .orElseThrow(() ->
	                            new IllegalArgumentException("找不到指定的會員優惠券")
	                    );

	    if (!memberId.equals(memberCoupon.getMemberId())) {
	        throw new IllegalArgumentException("這張優惠券不屬於目前登入會員");
	    }

	    if (memberCoupon.getUsedStatus() == null
	            || memberCoupon.getUsedStatus() != NOT_USED) {

	        throw new IllegalArgumentException("這張優惠券已使用或已被其他訂單占用");
	    }

	    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

	    if (now.isBefore(memberCoupon.getUsageStartTime())) {
	        throw new IllegalArgumentException("這張優惠券尚未開始使用");
	    }

	    if (now.isAfter(memberCoupon.getUsageEndTime())) {
	        throw new IllegalArgumentException("這張優惠券已過期");
	    }
	    CouponVO coupon = memberCoupon.getCoupon();
	    int discountAmount;
	    if (coupon.getDiscountType() != null
	            && coupon.getDiscountType() == 1) {
	        Integer fixedAmount = coupon.getDiscountAmount();
	        if (fixedAmount == null || fixedAmount <= 0) {
	            throw new IllegalStateException("固定金額優惠券設定錯誤");
	        }
	        discountAmount = Math.min(totalAmount, fixedAmount);
	    } else if (coupon.getDiscountType() != null
	            && coupon.getDiscountType() == 2) {

	        Integer payPercent = coupon.getDiscountPercent();

	        if (payPercent == null
	                || payPercent <= 0
	                || payPercent > 100) {
	            throw new IllegalStateException("百分比優惠券設定錯誤");
	        }
	        int paidAfterDiscount = (int) Math.round(totalAmount * (payPercent / 100.0));
	        discountAmount = totalAmount - paidAfterDiscount;
	    } else {
	        throw new IllegalStateException("優惠券折扣類型錯誤");
	    }
	    memberCoupon.setUsedStatus(USED);
	    memberCoupon.setUsedTime(now);
	    memberCouponRepository.save(memberCoupon);
	    return discountAmount;
	}
	
	@Transactional
	public void restoreCouponForUnpaidOrder(
	        Integer memberCouponId
	) {
	    if (memberCouponId == null) { return; }

	    MemberCouponVO memberCoupon = memberCouponRepository.findByIdForUpdate(memberCouponId).orElse(null);

	    if (memberCoupon == null) { return; }
	    memberCoupon.setUsedStatus(NOT_USED);
	    memberCoupon.setUsedTime(null);
	    memberCouponRepository.save(memberCoupon);
	}

	@Transactional(readOnly = true)
	public List<CouponVO> getAllCoupons() { // 查全部優惠券
		return couponRepository.findAll(Sort.by(Sort.Direction.ASC, "couponId"));
	}

	@Transactional(readOnly = true)
	public boolean isBirthdayCouponIssuable() {
		return couponRepository
				.findByCouponCodeAndIssueStatus(BIRTHDAY_CODE, ISSUE_ACTIVE)
				.isPresent();
	}

	@Transactional
	public void updateCouponIssueStatus( // 啟用或暫停發放
			Integer couponId, boolean enabled) {
		if (couponId == null) {
			throw new IllegalArgumentException("優惠券編號不可為空");
		}

		CouponVO coupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new IllegalArgumentException("找不到指定的優惠券"));

		coupon.setIssueStatus(enabled ? (byte) 1 : (byte) 0);

		couponRepository.save(coupon);
	}
	


	@Transactional
	public void deleteCoupon(Integer couponId) {
		if (couponId == null) {
			throw new IllegalArgumentException("優惠券編號不可為空");
		}

		CouponVO coupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new IllegalArgumentException("找不到指定的優惠券"));

		if (isSystemCoupon(coupon.getCouponCode())) {
			throw new IllegalArgumentException("新會員券與生日券屬於系統優惠券，不能刪除");
		}

		if (memberCouponRepository.existsByCoupon_CouponId(couponId)) {
			throw new IllegalArgumentException("此優惠券已發放給會員，為保留紀錄不能刪除，請改為暫停發放");
		}

		couponRepository.delete(coupon);
	}

	@Transactional
	public CouponVO createCoupon(CouponAdminForm form) {
		validateCouponForm(form, true);

		String normalizedCode = normalizeCouponCode(form.getCouponCode());

		if (couponRepository.findByCouponCodeIgnoreCase(normalizedCode).isPresent()) {
			throw new IllegalArgumentException("優惠券代碼已存在");
		}

		CouponVO coupon = new CouponVO();
		coupon.setCouponCode(normalizedCode);
		applyCouponForm(coupon, form);

		return couponRepository.save(coupon);
	}

	@Transactional
	public CouponVO updateCoupon(Integer couponId, CouponAdminForm form) {
		if (couponId == null) {
			throw new IllegalArgumentException("優惠券編號不可為空");
		}

		validateCouponForm(form, false);

		CouponVO coupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new IllegalArgumentException("找不到指定的優惠券"));

		applyCouponForm(coupon, form);
		return couponRepository.save(coupon);
	}

	private void validateCouponForm(CouponAdminForm form, boolean creating) {
		if (form == null) {
			throw new IllegalArgumentException("優惠券資料不可為空");
		}

		if (creating) {
			String code = normalizeCouponCode(form.getCouponCode());
			if (code.isBlank()) {
				throw new IllegalArgumentException("請填寫優惠券代碼");
			}
			if (code.length() > 50 || !code.matches("[A-Z0-9_]+")) {
				throw new IllegalArgumentException("優惠券代碼只能使用英文大寫、數字與底線，且不可超過 50 字");
			}
		}

		String name = form.getCouponName() == null ? "" : form.getCouponName().trim();
		if (name.isBlank()) {
			throw new IllegalArgumentException("請填寫優惠券名稱");
		}
		if (name.length() > 100) {
			throw new IllegalArgumentException("優惠券名稱不可超過 100 字");
		}

		Byte discountType = form.getDiscountType();
		if (discountType == null || (discountType != 1 && discountType != 2)) {
			throw new IllegalArgumentException("請選擇正確的折扣類型");
		}

		if (discountType == 1) {
			if (form.getDiscountAmount() == null || form.getDiscountAmount() <= 0) {
				throw new IllegalArgumentException("固定金額券必須填寫大於 0 的折抵金額");
			}
		} else {
			if (form.getDiscountPercent() == null
					|| form.getDiscountPercent() <= 0
					|| form.getDiscountPercent() > 100) {
				throw new IllegalArgumentException("百分比券的折後支付比例必須介於 1 到 100");
			}
		}

		if (form.getRemainingQuantity() != null && form.getRemainingQuantity() < 0) {
			throw new IllegalArgumentException("剩餘數量不可小於 0");
		}

		if (form.getDefaultValidDays() != null && form.getDefaultValidDays() <= 0) {
			throw new IllegalArgumentException("預設有效天數必須大於 0");
		}

		if (form.getIssueStatus() == null
				|| (form.getIssueStatus() != 0 && form.getIssueStatus() != 1)) {
			throw new IllegalArgumentException("發放狀態不正確");
		}
	}

	private void applyCouponForm(CouponVO coupon, CouponAdminForm form) {
		coupon.setCouponName(form.getCouponName().trim());

		String description = form.getDescription() == null
				? null
				: form.getDescription().trim();
		coupon.setDescription(description == null || description.isBlank() ? null : description);

		coupon.setDiscountType(form.getDiscountType());
		if (form.getDiscountType() == 1) {
			coupon.setDiscountAmount(form.getDiscountAmount());
			coupon.setDiscountPercent(null);
		} else {
			coupon.setDiscountAmount(null);
			coupon.setDiscountPercent(form.getDiscountPercent());
		}

		coupon.setRemainingQuantity(form.getRemainingQuantity());
		coupon.setDefaultValidDays(form.getDefaultValidDays());
		coupon.setIssueStatus(form.getIssueStatus());
	}

	private String normalizeCouponCode(String couponCode) {
		return couponCode == null
				? ""
				: couponCode.trim().toUpperCase(Locale.ROOT);
	}


	@Transactional(readOnly = true)
	public List<MemberVO> getEnabledMembersForCouponIssue() {
		return memberRepository.findByMemberStatusOrderByMemberIdAsc((byte) 1);
	}

	@Transactional(readOnly = true)
	public List<MemberCouponAdminDTO> getAllMemberCouponsForAdmin() {
		List<MemberCouponVO> memberCoupons =
				memberCouponRepository.findAllByOrderByClaimedTimeDesc();

		Set<Integer> memberIds = memberCoupons.stream()
				.map(MemberCouponVO::getMemberId)
				.collect(Collectors.toSet());

		Map<Integer, MemberVO> membersById = memberRepository
				.findAllById(memberIds)
				.stream()
				.collect(Collectors.toMap(
						MemberVO::getMemberId,
						Function.identity()
				));

		LocalDateTime now = nowTaipei();

		return memberCoupons.stream()
				.map(memberCoupon -> toAdminDTO(
						memberCoupon,
						membersById.get(memberCoupon.getMemberId()),
						now
				))
				.toList();
	}

	@Transactional
	public void issueCouponToMember(Integer couponId, Integer memberId) {
		CouponVO coupon = getIssuableCampaignCoupon(couponId);
		MemberVO member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("找不到指定會員"));

		if (member.getMemberStatus() == null || member.getMemberStatus() != 1) {
			throw new IllegalArgumentException("只能發給正常啟用的會員");
		}

		boolean issued = issueCampaignCoupon(coupon, member, nowTaipei());

		if (!issued) {
			throw new IllegalArgumentException("這位會員已取得此優惠券，或優惠券已發完");
		}
	}

	@Transactional
	public int issueCouponToAllEnabledMembers(Integer couponId) {
		CouponVO coupon = getIssuableCampaignCoupon(couponId);
		List<MemberVO> members =
				memberRepository.findByMemberStatusOrderByMemberIdAsc((byte) 1);

		int issuedCount = 0;
		LocalDateTime now = nowTaipei();

		for (MemberVO member : members) {
			Integer remainingQuantity = coupon.getRemainingQuantity();

			if (remainingQuantity != null && remainingQuantity <= 0) {
				break;
			}

			if (issueCampaignCoupon(coupon, member, now)) {
				issuedCount++;
			}
		}

		return issuedCount;
	}

	private boolean issueCampaignCoupon(
			CouponVO coupon,
			MemberVO member,
			LocalDateTime now
	) {
		String issuePeriod = "MANUAL-" + coupon.getCouponId();

		boolean alreadyIssued = memberCouponRepository
				.existsByMemberIdAndCoupon_CouponIdAndIssuePeriod(
						member.getMemberId(),
						coupon.getCouponId(),
						issuePeriod
				);

		if (alreadyIssued) {
			return false;
		}

		Integer remainingQuantity = coupon.getRemainingQuantity();
		if (remainingQuantity != null && remainingQuantity <= 0) {
			return false;
		}

		Integer validDays = coupon.getDefaultValidDays();
		if (validDays == null || validDays <= 0) {
			throw new IllegalArgumentException("請先設定預設有效天數，才能發放優惠券");
		}

		LocalDateTime usageEndTime = now.toLocalDate()
				.plusDays(validDays - 1L)
				.atTime(23, 59, 59);

		MemberCouponVO memberCoupon = new MemberCouponVO();
		memberCoupon.setMemberId(member.getMemberId());
		memberCoupon.setCoupon(coupon);
		memberCoupon.setIssuePeriod(issuePeriod);
		memberCoupon.setUsedStatus(NOT_USED);
		memberCoupon.setClaimedTime(now);
		memberCoupon.setUsageStartTime(now);
		memberCoupon.setUsageEndTime(usageEndTime);
		memberCoupon.setUsedTime(null);

		memberCouponRepository.save(memberCoupon);

		if (remainingQuantity != null) {
			coupon.setRemainingQuantity(remainingQuantity - 1);
			couponRepository.save(coupon);
		}

		memberNotifyService.createNotification(
				member.getMemberId(),
				"您獲得「"
						+ coupon.getCouponName()
						+ "」，請於 "
						+ usageEndTime.toLocalDate()
						+ " 前使用。"
		);

		return true;
	}

	private CouponVO getIssuableCampaignCoupon(Integer couponId) {
		if (couponId == null) {
			throw new IllegalArgumentException("優惠券編號不可為空");
		}

		CouponVO coupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new IllegalArgumentException("找不到指定的優惠券"));

		if (isSystemCoupon(coupon.getCouponCode())) {
			throw new IllegalArgumentException("新會員券與生日券請使用原本的專用發放流程");
		}

		if (coupon.getIssueStatus() == null || coupon.getIssueStatus() != ISSUE_ACTIVE) {
			throw new IllegalArgumentException("此優惠券目前已暫停發放");
		}

		return coupon;
	}

	private boolean isSystemCoupon(String couponCode) {
		return NEW_MEMBER_CODE.equalsIgnoreCase(couponCode)
				|| BIRTHDAY_CODE.equalsIgnoreCase(couponCode);
	}

	private MemberCouponAdminDTO toAdminDTO(
			MemberCouponVO memberCoupon,
			MemberVO member,
			LocalDateTime now
	) {
		CouponVO coupon = memberCoupon.getCoupon();
		MemberCouponAdminDTO dto = new MemberCouponAdminDTO();

		dto.setMemberCouponId(memberCoupon.getMemberCouponId());
		dto.setMemberId(memberCoupon.getMemberId());
		dto.setMemberName(member == null ? "未知會員" : member.getMemberName());
		dto.setMemberEmail(member == null ? "" : member.getMemberEmail());
		dto.setCouponId(coupon.getCouponId());
		dto.setCouponCode(coupon.getCouponCode());
		dto.setCouponName(coupon.getCouponName());
		dto.setIssuePeriod(memberCoupon.getIssuePeriod());
		dto.setClaimedTime(memberCoupon.getClaimedTime());
		dto.setUsageStartTime(memberCoupon.getUsageStartTime());
		dto.setUsageEndTime(memberCoupon.getUsageEndTime());
		dto.setUsedTime(memberCoupon.getUsedTime());
		dto.setDisplayStatus(resolveDisplayStatus(memberCoupon, now));

		return dto;
	}

	private LocalDateTime nowTaipei() {
		return LocalDateTime.now(TAIPEI_ZONE)
				.truncatedTo(ChronoUnit.SECONDS);
	}

	@Transactional
	public int createExpiringCouponNotifications() {

	    LocalDate targetDate =
	            LocalDate.now(
	                    ZoneId.of("Asia/Taipei")
	            ).plusDays(7);

	    LocalDateTime startTime =
	            targetDate.atStartOfDay();

	    LocalDateTime endTime =
	            targetDate.atTime(
	                    23,
	                    59,
	                    59
	            );

	    List<MemberCouponVO> expiringCoupons =
	            memberCouponRepository
	                    .findByUsedStatusAndUsageEndTimeBetween(
	                            NOT_USED,
	                            startTime,
	                            endTime
	                    );

	    int notificationCount = 0;

	    for (MemberCouponVO memberCoupon
	            : expiringCoupons) {

	        CouponVO coupon =
	                memberCoupon.getCoupon();

	        String notificationContent =
	                "您的「"
	                + coupon.getCouponName()
	                + "」將於 "
	                + memberCoupon
	                        .getUsageEndTime()
	                        .toLocalDate()
	                + " 到期，請記得於期限內使用。";

	        boolean alreadyNotified =
	                memberNotifyService
	                        .notificationExists(
	                                memberCoupon.getMemberId(),
	                                notificationContent
	                        );

	        if (alreadyNotified) {
	            continue;
	        }

	        memberNotifyService.createNotification(
	                memberCoupon.getMemberId(),
	                notificationContent
	        );

	        notificationCount++;
	    }

	    return notificationCount;
	}

	private MemberCouponDTO toDTO( // 轉換會員優惠券資料
			MemberCouponVO memberCoupon, LocalDateTime now) {
		CouponVO coupon = memberCoupon.getCoupon();

		MemberCouponDTO dto = new MemberCouponDTO();

		dto.setMemberCouponId(memberCoupon.getMemberCouponId());

		dto.setCouponCode(coupon.getCouponCode());

		dto.setCouponName(coupon.getCouponName());

		dto.setDescription(coupon.getDescription());

		dto.setDiscountType(coupon.getDiscountType());

		dto.setDiscountAmount(coupon.getDiscountAmount());

		dto.setDiscountPercent(coupon.getDiscountPercent());

		dto.setIssuePeriod(memberCoupon.getIssuePeriod());

		dto.setClaimedTime(memberCoupon.getClaimedTime());

		dto.setUsageStartTime(memberCoupon.getUsageStartTime());

		dto.setUsageEndTime(memberCoupon.getUsageEndTime());

		dto.setUsedTime(memberCoupon.getUsedTime());

		dto.setDisplayStatus(resolveDisplayStatus(memberCoupon, now));

		return dto;
	}

	private String resolveDisplayStatus(MemberCouponVO memberCoupon, LocalDateTime now) {
		Byte usedStatus = memberCoupon.getUsedStatus();

		if (usedStatus != null && usedStatus == USED) {

			return "USED";
		}

		if (now.isAfter(memberCoupon.getUsageEndTime())) {
			return "EXPIRED";
		}

		if (now.isBefore(memberCoupon.getUsageStartTime())) {
			return "NOT_STARTED";
		}

		return "AVAILABLE";
	}
}