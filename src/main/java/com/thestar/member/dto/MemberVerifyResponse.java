package com.thestar.member.dto;

public class MemberVerifyResponse {

    private boolean ok;
    private String message;

    public MemberVerifyResponse() {
    }

    public MemberVerifyResponse(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
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
}
