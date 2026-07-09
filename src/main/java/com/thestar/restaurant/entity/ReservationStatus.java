package com.thestar.restaurant.entity;

/*
 * 對應資料庫中 RESERVATION_STATUS 的 ENUM 型態
 * 資料庫定義：ENUM('BOOKED', 'FINISHED', 'CANCELED')
 */
public enum ReservationStatus {
    BOOKED,
    FINISHED,
    CANCELED
}
