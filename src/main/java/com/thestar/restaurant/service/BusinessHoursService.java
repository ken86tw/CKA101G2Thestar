package com.thestar.restaurant.service;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.restaurant.entity.BusinessHoursVO;
import com.thestar.restaurant.repository.BusinessHoursRepository;

@Service
public class BusinessHoursService {

    @Autowired
    BusinessHoursRepository repository;

    @Autowired
    private SessionFactory sessionFactory;

    public void addBusinessHours(BusinessHoursVO businessHoursVO) {
        repository.save(businessHoursVO);
    }

    public void updateBusinessHours(BusinessHoursVO businessHoursVO) {
        repository.save(businessHoursVO);
    }

    public void deleteBusinessHours(Integer sessionId) {
        if (repository.existsById(sessionId))
            repository.deleteById(sessionId);
    }

    public BusinessHoursVO getOneBusinessHours(Integer sessionId) {
        Optional<BusinessHoursVO> optional = repository.findById(sessionId);
        return optional.orElse(null);
    }

    public List<BusinessHoursVO> getAll() {
        return repository.findAllOrderByStartTime();
    }

    // 查詢目前正在營業的時段
    public List<BusinessHoursVO> getActiveSession(Time currentTime) {
        return repository.findActiveSession(currentTime);
    }
}
