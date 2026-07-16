package com.thestar.restaurant.dto;

public class SessionStatusDTO {
    private Integer sessionId;
    private String startTime;  // 轉成 "11:30" 格式方便前端顯示
    private String endTime;    // 轉成 "13:30" 格式
    private boolean available; // 是否可訂位

    public SessionStatusDTO(Integer sessionId, java.sql.Time startTime, java.sql.Time endTime, boolean available) {
        this.sessionId = sessionId;
        this.startTime = startTime != null ? startTime.toString().substring(0, 5) : "";
        this.endTime = endTime != null ? endTime.toString().substring(0, 5) : "";
        this.available = available;
    }

    // Getters and Setters
    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}