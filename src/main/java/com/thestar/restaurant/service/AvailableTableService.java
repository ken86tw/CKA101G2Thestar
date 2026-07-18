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
import com.thestar.restaurant.repository.RestaurantTableRepository;

@Service
public class AvailableTableService {

    @Autowired
    private AvailableTableRepository repository;

    @Autowired
    private BusinessHoursRepository businessHoursRepository; 
    
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

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
    
    /**
     * 3. 初始化未來 30 天的桌況資料表
     * 💡 換成 LocalDate 後，日期加減變得超級簡單，不再需要麻煩的 Calendar 與 System.currentTimeMillis()！
     */
    @Transactional
    public void initializeMonthlyTables() {
        LocalDate today = LocalDate.now(); // 👈 取得今天的 LocalDate
        List<BusinessHoursVO> businessHours = businessHoursRepository.findAll();
        
        
        
        for (int i = 0; i <= 31; i++) {
            LocalDate targetDate = today.plusDays(i); // 👈 直接加天數，一行搞定！
            
            for (int sessionId = 1; sessionId <= businessHours.size(); sessionId++) {
                AvailableTableId id = new AvailableTableId(targetDate, sessionId);
                boolean exists = repository.existsById(id);
                
                if (!exists) {
                	
                    AvailableTableVO newTable = new AvailableTableVO();
                    newTable.setId(id); 
                    
                    BusinessHoursVO bhVO = businessHoursRepository.findById(sessionId).orElse(null);
                    
                    Integer largeTableCount = restaurantTableRepository.findById(1)
                    	    .orElseThrow(() -> new RuntimeException("找不到該桌況資料"))
                    	    .getTableTypeCount();    
                    
                    Integer smallTableCount = restaurantTableRepository.findById(2)
                    	    .orElseThrow(() -> new RuntimeException("找不到該桌況資料"))
                    	    .getTableTypeCount();    
                    
                    
                    
                    if (bhVO != null) {
                        newTable.setBusinessHoursVO(bhVO);
                        newTable.setLargeTableCount(largeTableCount);  
                        newTable.setSmallTableCount(smallTableCount);  
                        
                        
                        repository.save(newTable);
                    } else {
                        System.out.println("警告：找不到 SESSION_ID = " + sessionId + " 的營業時間設定，跳過初始化。");
                    }
                }
            }
        }
    }
}