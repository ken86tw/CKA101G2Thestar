package com.thestar.member.dto;

public class MemberRegisterResponse {

    private boolean ok;
    private String message;
    private boolean mailSent;
    private String devVerifyUrl;

    public MemberRegisterResponse() {
    }

    public MemberRegisterResponse(boolean ok, String message, boolean mailSent, String devVerifyUrl) {
        this.ok = ok;
        this.message = message;
        this.mailSent = mailSent;
        this.devVerifyUrl = devVerifyUrl;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMailSent() {
        return mailSent;
    }

    public void setMailSent(boolean mailSent) {
        this.mailSent = mailSent;
    }

    public String getDevVerifyUrl() {
        return devVerifyUrl;
    }

    public void setDevVerifyUrl(String devVerifyUrl) {
        this.devVerifyUrl = devVerifyUrl;
    }
}
