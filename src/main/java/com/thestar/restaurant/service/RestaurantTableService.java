package com.thestar.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.restaurant.entity.RestaurantTableVO;
import com.thestar.restaurant.repository.RestaurantTableRepository;

@Service
public class RestaurantTableService {

    @Autowired
    RestaurantTableRepository repository;

    public void addRestaurantTable(RestaurantTableVO restaurantTableVO) {
        repository.save(restaurantTableVO);
    }

    public void updateRestaurantTable(RestaurantTableVO restaurantTableVO) {
        repository.save(restaurantTableVO);
    }

    public void deleteRestaurantTable(Integer tableType) {
        if (repository.existsById(tableType))
            repository.deleteById(tableType);
    }

    public RestaurantTableVO getOneRestaurantTable(Integer tableType) {
        Optional<RestaurantTableVO> optional = repository.findById(tableType);
        return optional.orElse(null);
    }

    public List<RestaurantTableVO> getAll() {
        return repository.findAll();
    }
}
