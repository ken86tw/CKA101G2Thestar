package com.thestar.member.service;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.repository.MemberRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class MemberAdminService {

    public static final byte STATUS_UNVERIFIED = 0;
    public static final byte STATUS_ENABLED = 1;
    public static final byte STATUS_SUSPENDED = 2;

    private final MemberRepository memberRepository;

    public MemberAdminService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberVO> searchMembers(
            String memberName,
            String memberEmail,
            Byte status
    ) {
        String normalizedName = normalize(memberName);
        String normalizedEmail = normalize(memberEmail);

        return memberRepository
                .findAll(Sort.by(Sort.Direction.DESC, "memberId"))
                .stream()
                .filter(member ->
                        status == null
                        || status.equals(member.getMemberStatus())
                )
                .filter(member ->
                        normalizedName.isBlank()
                        || contains(member.getMemberName(), normalizedName)
                )
                .filter(member ->
                        normalizedEmail.isBlank()
                        || contains(member.getMemberEmail(), normalizedEmail)
                )
                .toList();
    }

    @Transactional
    public void updateStatus(Integer memberId, Byte newStatus) {
        if (memberId == null) {
            throw new IllegalArgumentException("會員編號不可為空");
        }

        if (newStatus == null
                || (newStatus != STATUS_ENABLED && newStatus != STATUS_SUSPENDED)) {
            throw new IllegalArgumentException("會員狀態只能設定為啟用或停權");
        }

        MemberVO member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("找不到指定會員"));

        Byte oldStatus = member.getMemberStatus();

        if (oldStatus == null) {
            oldStatus = STATUS_UNVERIFIED;
        }

        if (oldStatus == STATUS_UNVERIFIED) {
            throw new IllegalArgumentException("尚未完成信箱驗證的會員不能由後台直接啟用或停權");
        }

        if (oldStatus.equals(newStatus)) {
            throw new IllegalArgumentException("會員目前已是此狀態");
        }

        member.setMemberStatus(newStatus);
        memberRepository.save(member);
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean contains(String value, String keyword) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
