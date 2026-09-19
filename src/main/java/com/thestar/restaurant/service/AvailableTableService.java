package com.thestar.restaurant.service;

import java.time.LocalDate; // 👈 1. 換成新版的 LocalDate
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.restaurant.entity.AvailableTableId;
import com.thestar.restaurant.entity.AvailableTableVO;
import com.thestar.restaurant.entity.BusinessHoursVO;
import com.thestar.restaurant.repository.AvailableTableRepository;
import com.thestar.restaurant.repository.BusinessHoursRepository;
import com.thestar.restaurant.repository.RestaurantReservationRepository;
import com.thestar.restaurant.repository.RestaurantTableRepository;

@Service
public class AvailableTableService {

    @Autowired
    private AvailableTableRepository repository;

    @Autowired
    private BusinessHoursRepository businessHoursRepository; 
    
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    
    @Autowired
    private RestaurantReservationRepository restaurantReservationRepository;
    

    public void addAvailableTable(AvailableTableVO availableTableVO) {
        repository.save(availableTableVO);
    }

    public void updateAvailableTable(AvailableTableVO availableTableVO) {
        repository.save(availableTableVO);
    }

    // 刪除：傳入兩個主鍵欄位，組成 AvailableTableId 後再刪除
    public void deleteAvailableTable(LocalDate date, Integer sessionId) { // 👈 換成 LocalDate
        AvailableTableId id = new AvailableTableId(date, sessionId);
        if (repository.existsById(id))
            repository.deleteById(id);
    }

    // 查單筆：傳入兩個主鍵欄位，組成 AvailableTableId 後查詢
    public AvailableTableVO getOneAvailableTable(LocalDate date, Integer sessionId) { // 👈 換成 LocalDate
        AvailableTableId id = new AvailableTableId(date, sessionId);
        Optional<AvailableTableVO> optional = repository.findById(id);
        return optional.orElse(null);
    }

    // 查全部
    public List<AvailableTableVO> getAll() {
        return repository.findAll();
    }

    // 查某天的所有時段餘量（前台選位用）
    public List<AvailableTableVO> getByDate(LocalDate date) { // 👈 換成 LocalDate
        return repository.findById_Date(date);
    }

    // 查某天還有大桌的時段
    public List<AvailableTableVO> getAvailableLargeTables(LocalDate date) { // 👈 換成 LocalDate
        return repository.findAvailableLargeTablesByDate(date);
    }

    // 查某天還有小桌的時段
    public List<AvailableTableVO> getAvailableSmallTables(LocalDate date) { // 👈 換成 LocalDate
        return repository.findAvailableSmallTablesByDate(date);
    }

 // 訂位成功後，扣減大桌數量（防超賣版）
    @Transactional
    public void decreaseLargeTableCount(LocalDate date, Integer sessionId) {
        int updatedRows = repository.decreaseLargeTableCount(date, sessionId);
        if (updatedRows == 0) {
            throw new RuntimeException("非常抱歉，該時段的大桌已被訂滿！");
        }
    }

    // 訂位成功後，扣減小桌數量（防超賣版）
    @Transactional
    public void decreaseSmallTableCount(LocalDate date, Integer sessionId) {
        int updatedRows = repository.decreaseSmallTableCount(date, sessionId);
        if (updatedRows == 0) {
            throw new RuntimeException("非常抱歉，該時段的小桌已被訂滿！");
        }
    }

    // 取消訂位後，歸還大桌數量
    public void restoreLargeTableCount(LocalDate date, Integer sessionId) { // 👈 換成 LocalDate
        AvailableTableVO vo = getOneAvailableTable(date, sessionId);
        if (vo != null)
            repository.updateLargeTableCount(date, sessionId, vo.getLargeTableCount() + 1);
    }

    // 取消訂位後，歸還小桌數量
    public void restoreSmallTableCount(LocalDate date, Integer sessionId) { // 👈 換成 LocalDate
        AvailableTableVO vo = getOneAvailableTable(date, sessionId);
        if (vo != null)
            repository.updateSmallTableCount(date, sessionId, vo.getSmallTableCount() + 1);
    }
    
    // =====================================================================
    // 新增：前端預約流程連動邏輯（依人數分流大小桌）
    // =====================================================================

    /**
     * 根據人數撈出「還有空桌」的日期清單
     * 1~4人檢查小桌，5~10人檢查大桌
     */
    public List<LocalDate> getAvailableDatesByGuests(int guests) { // 👈 2. 這裡回傳換成 List<LocalDate>
        return repository.findAvailableDatesByGuests(guests);
    }

    /**
     * 根據特定日期 + 人數，撈出「還有空桌」的時段資料 (AvailableTableVO 清單)
     * 1~4人檢查小桌，5~10人檢查大桌
     */
    public List<AvailableTableVO> getAvailableSessionsByDateAndGuests(LocalDate date, int guests) { // 👈 換成 LocalDate
        if (date == null) {
            return java.util.Collections.emptyList();
        }
        return repository.findAvailableSessionsByDateAndGuests(date, guests);
    }
    
    
    private int compareSession(Integer s1, Integer s2, List<BusinessHoursVO> sortedList) {
        int idx1 = -1;
        int idx2 = -1;
        
        // 在已經依照時間排好序的清單中，找出這兩個時段的索引位置
        for (int i = 0; i < sortedList.size(); i++) {
            if (sortedList.get(i).getSessionId().equals(s1)) idx1 = i;
            if (sortedList.get(i).getSessionId().equals(s2)) idx2 = i;
        }
        
        // 回傳比對結果：負數代表 s1 在 s2 之前，0 代表相同，正數代表 s1 在 s2 之後
        return Integer.compare(idx1, idx2);
    }
    @Transactional
    public void closeAvailableTablesRange(LocalDate startDate, Integer startSessionId, LocalDate endDate, Integer endSessionId) {
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("開始日期不能大於結束日期");

        List<BusinessHoursVO> allSessions = businessHoursRepository.findAll();
        allSessions.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));

        LocalDate curDate = startDate;
        while (!curDate.isAfter(endDate)) {
            for (BusinessHoursVO bhVO : allSessions) {
                if (bhVO.getIsAvailable() == null || !bhVO.getIsAvailable()) continue;

                boolean isWithinRange = true;
                if (curDate.equals(startDate) && compareSession(bhVO.getSessionId(), startSessionId, allSessions) < 0) isWithinRange = false;
                if (curDate.equals(endDate) && compareSession(bhVO.getSessionId(), endSessionId, allSessions) > 0) isWithinRange = false;

                if (isWithinRange) {
                    // 檢查是否有有效訂位
                	long count = restaurantReservationRepository.countActiveReservations(java.sql.Date.valueOf(curDate), bhVO.getSessionId());

                	if (count > 0) {
                	    // 💡 拋出明確的錯誤訊息，會被 Controller 捕捉並顯示在網頁的 errorMessage 中
                	    throw new IllegalStateException("無法關閉訂位！在 " + curDate + " 的該時段內，尚有 " + count + " 筆訂位狀態為 BOOKED 或進行中的訂單，請先處理這些訂單。");
                	}
                    AvailableTableId id = new AvailableTableId(curDate, bhVO.getSessionId());
                    AvailableTableVO vo = repository.findById(id).orElse(new AvailableTableVO());
                    if (vo.getId() == null) {
                        vo.setId(id);
                        vo.setBusinessHoursVO(bhVO);
                    }
                    vo.setLargeTableCount(0);
                    vo.setSmallTableCount(0);
                    repository.save(vo);
                }
            }
            curDate = curDate.plusDays(1);
        }
    }
    /**
     * 3. 初始化未來 30 天的桌況資料表
     * 💡 換成 LocalDate 後，日期加減變得超級簡單，不再需要麻煩的 Calendar 與 System.currentTimeMillis()！
     */
    @Transactional
    public void initializeMonthlyTables() {
        LocalDate today = LocalDate.now(); 
        // 1. 撈出所有營業時段（包含上架與下架）
        List<BusinessHoursVO> businessHours = businessHoursRepository.findAll();
        
        // 2. 提早查出桌況配置（避免放在雙重迴圈內重複查詢，提升效能）
        Integer largeTableCount = restaurantTableRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("找不到該桌況資料"))
                .getTableTypeCount();    
        
        Integer smallTableCount = restaurantTableRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("找不到該桌況資料"))
                .getTableTypeCount();    
        
        // 外層迴圈：未來的 31 天
        for (int i = 0; i <= 31; i++) {
            LocalDate targetDate = today.plusDays(i); 
            
            // 內層迴圈：改為直接遍歷營業時段物件
            for (BusinessHoursVO bhVO : businessHours) {
                
                // === 核心修改：如果該時段狀態是下架 (false) 或為 null，就不初始化該筆資料，直接跳過 ===
                if (bhVO.getIsAvailable() == null || !bhVO.getIsAvailable()) {
                    continue; 
                }
                
                Integer sessionId = bhVO.getSessionId();
                AvailableTableId id = new AvailableTableId(targetDate, sessionId);
                boolean exists = repository.existsById(id);
                
                // 如果該日期的該時段還沒有初始化過，才新增
                if (!exists) {
                    AvailableTableVO newTable = new AvailableTableVO();
                    newTable.setId(id); 
                    
                    // 這裡可以直接使用外層迴圈帶進來的 bhVO，不需再重新 findById(sessionId)
                    newTable.setBusinessHoursVO(bhVO);
                    newTable.setLargeTableCount(largeTableCount);  
                    newTable.setSmallTableCount(smallTableCount);  
                    
                    repository.save(newTable);
                }
            }
        }
    }}