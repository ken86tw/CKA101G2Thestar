package com.thestar.member.service;

import com.thestar.member.dto.MemberProfileUpdateRequest;
import com.thestar.member.dto.MemberRegisterRequest;
import com.thestar.member.dto.MemberRegisterResponse;
import com.thestar.member.dto.MemberVerifyResponse;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class MemberAuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern TAIWAN_PHONE_PATTERN = Pattern.compile("^09\\d{8}$");
    private static final long MAX_PICTURE_SIZE = 5L * 1024L * 1024L;
    private static final long VERIFY_EXPIRE_MINUTES = 10L;

    private final MemberRepository memberRepository;
    private final MemberVerificationMailService verificationMailService;

    public MemberAuthService(MemberRepository memberRepository,
                             MemberVerificationMailService verificationMailService) {
        this.memberRepository = memberRepository;
        this.verificationMailService = verificationMailService;
    }

    @Transactional
    public MemberRegisterResponse register(MemberRegisterRequest request) throws IOException {
        String memberName = trim(request.getMemberName());
        String memberEmail = trim(request.getMemberEmail()).toLowerCase();
        String memberPassword = request.getMemberPassword() == null ? "" : request.getMemberPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();
        String memberPhone = trim(request.getMemberPhone());
        String memberAddress = trim(request.getMemberAddress());
        Byte memberGender = request.getMemberGender() == null ? 2 : request.getMemberGender();
        MultipartFile memberPictureFile = request.getMemberPicture();

        // 這段保留你原本 Servlet insert() 的判斷：必填、手機格式、性別、圖片大小。
        validateRegisterData(
                memberName,
                memberEmail,
                memberPassword,
                confirmPassword,
                memberPhone,
                memberAddress,
                request.getMemberBirthday(),
                memberGender,
                memberPictureFile
        );

        if (memberRepository.existsByMemberEmailIgnoreCase(memberEmail)) {
            throw new IllegalArgumentException("新增失敗，可能是信箱重複");
        }

        MemberVO member = new MemberVO();
        member.setMemberName(memberName);
        member.setMemberEmail(memberEmail);
        member.setMemberPassword(memberPassword);
        member.setMemberPhone(memberPhone);
        member.setMemberAddress(memberAddress);
        member.setMemberBirthday(request.getMemberBirthday());
        member.setMemberGender(memberGender);
        member.setMemberStatus((byte) 0);

        if (memberPictureFile != null && !memberPictureFile.isEmpty()) {
            member.setMemberPicture(memberPictureFile.getBytes());
        }

        String verifyToken = createVerifyToken();
        member.setVerifyToken(verifyToken);
        member.setVerifyExpireTime(LocalDateTime.now().plusMinutes(VERIFY_EXPIRE_MINUTES));

        MemberVO savedMember = memberRepository.save(member);

        String verifyUrl = verificationMailService.buildVerifyUrl(savedMember.getVerifyToken());
        System.out.println("會員驗證連結：");
        System.out.println(verifyUrl);

        boolean mailSent = verificationMailService.sendVerifyMail(savedMember, verifyUrl);

        String message = mailSent
                ? "註冊成功，請到信箱收取驗證信"
                : "註冊成功，但驗證信寄送失敗，請先使用畫面上的開發測試驗證連結";

        return new MemberRegisterResponse(true, message, mailSent, verifyUrl);
    }

    @Transactional
    public MemberVerifyResponse verifyEmail(String token) {
        String cleanToken = trim(token);

        if (cleanToken.isEmpty()) {
            throw new IllegalArgumentException("驗證連結無效");
        }

        MemberVO member = memberRepository.findByVerifyToken(cleanToken)
                .orElseThrow(() -> new IllegalArgumentException("驗證連結不存在，或帳號已經驗證過"));

        if (member.getVerifyExpireTime() != null && member.getVerifyExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("驗證連結已過期，請重新註冊或重新寄送驗證信");
        }

        member.setMemberStatus((byte) 1);
        member.setVerifyToken(null);
        member.setVerifyExpireTime(null);
        memberRepository.save(member);

        return new MemberVerifyResponse(true, "會員信箱驗證成功，帳號已啟用");
    }


    @Transactional
    public MemberRegisterResponse resendVerificationMail(String email) {
        String cleanEmail = trim(email).toLowerCase();

        if (cleanEmail.isEmpty()) {
            throw new IllegalArgumentException("請輸入會員信箱");
        }

        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException("信箱格式錯誤");
        }

        MemberVO member = memberRepository.findByMemberEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("查無此會員信箱"));

        if (member.getMemberStatus() != null && member.getMemberStatus() == 1) {
            throw new IllegalArgumentException("此帳號已完成信箱驗證，請直接登入");
        }

        if (member.getMemberStatus() != null && member.getMemberStatus() == 2) {
            throw new IllegalArgumentException("帳號已停用，請聯絡客服");
        }

        String verifyToken = createVerifyToken();
        member.setVerifyToken(verifyToken);
        member.setVerifyExpireTime(LocalDateTime.now().plusMinutes(VERIFY_EXPIRE_MINUTES));

        MemberVO savedMember = memberRepository.save(member);

        String verifyUrl = verificationMailService.buildVerifyUrl(savedMember.getVerifyToken());
        System.out.println("重新寄送會員驗證連結：");
        System.out.println(verifyUrl);

        boolean mailSent = verificationMailService.sendVerifyMail(savedMember, verifyUrl);

        String message = mailSent
                ? "驗證信已重新寄出，請到信箱收信"
                : "驗證信寄送失敗，請稍後再試";

        return new MemberRegisterResponse(true, message, mailSent, verifyUrl);
    }

    @Transactional(readOnly = true)
    public MemberVO login(String email, String password) {
        String cleanEmail = email == null ? "" : email.trim();
        String cleanPassword = password == null ? "" : password.trim();

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            throw new IllegalArgumentException("請輸入信箱與密碼");
        }

        MemberVO member = memberRepository.findByMemberEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("信箱或密碼錯誤"));

        if (!cleanPassword.equals(member.getMemberPassword())) {
            throw new IllegalArgumentException("信箱或密碼錯誤");
        }

        if (member.getMemberStatus() == null || member.getMemberStatus() == 0) {
            throw new IllegalStateException("帳號尚未完成信箱驗證");
        }

        if (member.getMemberStatus() == 2) {
            throw new IllegalStateException("帳號已停用，請聯絡客服");
        }

        return member;
    }

    @Transactional(readOnly = true)
    public MemberVO getMemberById(Integer memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("請先登入會員");
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));
    }

    @Transactional
    public MemberVO updateProfile(Integer memberId, MemberProfileUpdateRequest request) {
        if (memberId == null) {
            throw new IllegalArgumentException("請先登入會員");
        }

        MemberVO member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));

        String name = trim(request.getMemberName());
        String phone = trim(request.getMemberPhone());
        String address = trim(request.getMemberAddress());

        if (name.isEmpty()) {
            throw new IllegalArgumentException("姓名不可空白");
        }

        if (!phone.isEmpty() && !TAIWAN_PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("手機格式錯誤，請輸入 09 開頭共 10 碼");
        }

        member.setMemberName(name);
        member.setMemberPhone(phone);
        member.setMemberAddress(address);
        member.setMemberGender(request.getMemberGender());

        return memberRepository.save(member);
    }

    @Transactional
    public MemberVO updateProfilePicture(Integer memberId, MultipartFile picture) throws IOException {
        if (memberId == null) {
            throw new IllegalArgumentException("請先登入會員");
        }

        if (picture == null || picture.isEmpty()) {
            throw new IllegalArgumentException("請選擇要上傳的照片");
        }

        validatePicture(picture);

        MemberVO member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));

        member.setMemberPicture(picture.getBytes());

        return memberRepository.save(member);
    }

    private void validateRegisterData(String memberName,
                                      String memberEmail,
                                      String memberPassword,
                                      String confirmPassword,
                                      String memberPhone,
                                      String memberAddress,
                                      LocalDate memberBirthday,
                                      Byte memberGender,
                                      MultipartFile memberPicture) {
        if (memberName.isEmpty()) {
            throw new IllegalArgumentException("會員姓名請勿空白");
        }

        if (memberEmail.isEmpty()) {
            throw new IllegalArgumentException("會員信箱請勿空白");
        }

        if (!EMAIL_PATTERN.matcher(memberEmail).matches()) {
            throw new IllegalArgumentException("會員信箱格式不正確");
        }

        if (memberPassword.isBlank()) {
            throw new IllegalArgumentException("會員密碼請勿空白");
        }
        
        if (confirmPassword.isBlank()) {
            throw new IllegalArgumentException("確認密碼請勿空白");
        }

        // confirmPassword 是 Vue 版多加的前端保護，不會改掉你原本密碼必填判斷。
        if (!memberPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("兩次輸入的密碼不一致");
        }

        if (memberPhone.isEmpty()) {
            throw new IllegalArgumentException("會員手機請勿空白");
        }

        if (!TAIWAN_PHONE_PATTERN.matcher(memberPhone).matches()) {
            throw new IllegalArgumentException("手機格式錯誤，請輸入 09 開頭的 10 碼手機號碼");
        }

        if (memberAddress.isEmpty()) {
            throw new IllegalArgumentException("會員地址請勿空白");
        }
        
        if (memberBirthday == null) {
            throw new IllegalArgumentException("會員生日請勿空白");
        }

        if (!isValidChoice(memberGender)) {
            throw new IllegalArgumentException("會員性別格式不正確");
        }

        if (memberPicture != null && !memberPicture.isEmpty()) {
            validatePicture(memberPicture);
        }
    }

    private void validatePicture(MultipartFile picture) {
        if (picture.getSize() > MAX_PICTURE_SIZE) {
            throw new IllegalArgumentException("圖片大小不可超過 5MB");
        }

        Set<String> allowTypes = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
        String contentType = picture.getContentType();

        if (contentType != null && !contentType.isBlank() && !allowTypes.contains(contentType)) {
            throw new IllegalArgumentException("照片格式只支援 JPG、PNG、GIF、WEBP");
        }
    }

    private String createVerifyToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isValidChoice(Byte value) {
        return value != null && (value == 0 || value == 1 || value == 2);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
