package com.thestar.member.controller;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberAuthService;
import com.thestar.member.dto.MemberLoginRequest;
import com.thestar.member.dto.MemberProfileDTO;
import com.thestar.member.dto.MemberProfileUpdateRequest;
import com.thestar.member.dto.MemberRegisterRequest;
import com.thestar.member.dto.MemberRegisterResponse;
import com.thestar.member.dto.MemberSessionDTO;
import com.thestar.member.dto.MemberVerifyResponse;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class MemberAuthController {

    private final MemberAuthService memberAuthService;

    public MemberAuthController(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }


    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@ModelAttribute MemberRegisterRequest request) {
        try {
            MemberRegisterResponse response = memberAuthService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "會員照片讀取失敗"));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        try {
            MemberVerifyResponse response = memberAuthService.verifyEmail(token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String memberEmail = request.get("memberEmail");
            MemberRegisterResponse response = memberAuthService.resendVerificationMail(memberEmail);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginRequest request, HttpSession session) {
        try {
            MemberVO member = memberAuthService.login(
                    request.getMemberEmail(),
                    request.getMemberPassword()
            );

            session.setAttribute("loginMember", member);

            return ResponseEntity.ok(MemberSessionDTO.from(member));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public MemberSessionDTO status(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");

        if (member == null || member.getMemberId() == null) {
            return MemberSessionDTO.guest();
        }

        return MemberSessionDTO.from(member);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.removeAttribute("loginMember");
        session.removeAttribute("loginEmployee");
        return Map.of("ok", true);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null || loginMember.getMemberId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入會員"));
        }

        MemberVO member = memberAuthService.getMemberById(loginMember.getMemberId());

        return ResponseEntity.ok(MemberProfileDTO.from(member));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody MemberProfileUpdateRequest request,
                                           HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null || loginMember.getMemberId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入會員"));
        }

        try {
            MemberVO updatedMember = memberAuthService.updateProfile(loginMember.getMemberId(), request);

            session.setAttribute("loginMember", updatedMember);

            return ResponseEntity.ok(MemberProfileDTO.from(updatedMember));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/picture")
    public ResponseEntity<?> profilePicture(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null || loginMember.getMemberId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入會員"));
        }

        MemberVO member = memberAuthService.getMemberById(loginMember.getMemberId());
        byte[] picture = member.getMemberPicture();

        if (picture == null || picture.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "尚未上傳會員照片"));
        }

        return ResponseEntity.ok()
                .contentType(detectPictureMediaType(picture))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(picture);
    }

    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(@RequestParam("picture") MultipartFile picture,
                                                  HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null || loginMember.getMemberId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入會員"));
        }

        try {
            MemberVO updatedMember = memberAuthService.updateProfilePicture(loginMember.getMemberId(), picture);
            session.setAttribute("loginMember", updatedMember);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "照片儲存失敗"));
        }
    }

    private MediaType detectPictureMediaType(byte[] picture) {
        if (picture.length >= 3
                && (picture[0] & 0xff) == 0xff
                && (picture[1] & 0xff) == 0xd8
                && (picture[2] & 0xff) == 0xff) {
            return MediaType.IMAGE_JPEG;
        }

        if (picture.length >= 8
                && (picture[0] & 0xff) == 0x89
                && picture[1] == 0x50
                && picture[2] == 0x4e
                && picture[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }

        if (picture.length >= 6
                && picture[0] == 0x47
                && picture[1] == 0x49
                && picture[2] == 0x46) {
            return MediaType.IMAGE_GIF;
        }

        if (picture.length >= 12
                && picture[0] == 0x52
                && picture[1] == 0x49
                && picture[2] == 0x46
                && picture[3] == 0x46
                && picture[8] == 0x57
                && picture[9] == 0x45
                && picture[10] == 0x42
                && picture[11] == 0x50) {
            return MediaType.valueOf("image/webp");
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
