package com.thestar.restaurant.repository;

import java.time.LocalDate; // 👈 1. 換成新版的 LocalDate
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.restaurant.entity.AvailableTableId;
import com.thestar.restaurant.entity.AvailableTableVO;

public interface AvailableTableRepository extends JpaRepository<AvailableTableVO, AvailableTableId> {

    // 查詢某一天的所有時段餘量
    List<AvailableTableVO> findById_Date(LocalDate date); // 👈 換成 LocalDate

    // 查詢某時段在某天的餘量
    Optional<AvailableTableVO> findById_DateAndId_SessionId(LocalDate date, Integer sessionId); // 👈 換成 LocalDate

    // 更新指定日期+時段的大桌數量
    @Transactional
    @Modifying
    @Query("update AvailableTableVO set largeTableCount = ?3 where id.date = ?1 and id.sessionId = ?2")
    void updateLargeTableCount(LocalDate date, Integer sessionId, int newCount); // 👈 換成 LocalDate

    // 更新指定日期+時段的小桌數量
    @Transactional
    @Modifying
    @Query("update AvailableTableVO set smallTableCount = ?3 where id.date = ?1 and id.sessionId = ?2")
    void updateSmallTableCount(LocalDate date, Integer sessionId, int newCount); // 👈 換成 LocalDate

    // 查詢某天還有大桌剩餘的時段
    @Query("from AvailableTableVO where id.date = ?1 and largeTableCount > 0")
    List<AvailableTableVO> findAvailableLargeTablesByDate(LocalDate date); // 👈 換成 LocalDate

    // 查詢某天還有小桌剩餘的時段
    @Query("from AvailableTableVO where id.date = ?1 and smallTableCount > 0")
    List<AvailableTableVO> findAvailableSmallTablesByDate(LocalDate date); // 👈 換成 LocalDate
    
    // =====================================================================
    // 新增：訂位前端聯動查詢（依人數過濾）
    // =====================================================================

    /**
     * 1. 根據人數，撈出「還有空桌」的日期列表
     */
    @Query(value = "select distinct a.date from available_table a " +
    	       "where a.date >= CURRENT_DATE " +
    	       "  and a.date <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) " +
    	       "  and ((:guests <= 4 and a.small_table_count > 0) " +
    	       "   or (:guests > 4 and :guests <= 10 and a.large_table_count > 0)) " +
    	       "order by a.date asc", nativeQuery = true)
    List<LocalDate> findAvailableDatesByGuests(@Param("guests") int guests); // 👈 2. 這裡改成 List<LocalDate>，解決崩潰主因！

    /**
     * 2. 根據特定日期 + 人數，撈出「還有空桌」的時段資料
     */
    @Query("from AvailableTableVO a " +
           "where a.id.date = :date " +
           "  and ((:guests <= 4 and a.smallTableCount > 0) " +
           "   or (:guests > 4 and :guests <= 10 and a.largeTableCount > 0)) " +
           "order by a.id.sessionId asc")
    List<AvailableTableVO> findAvailableSessionsByDateAndGuests(@Param("date") LocalDate date, @Param("guests") int guests); // 👈 換成 LocalDate
}