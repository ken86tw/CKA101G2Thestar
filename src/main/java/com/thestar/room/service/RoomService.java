package com.thestar.room.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thestar.room.entity.RoomVO;
import com.thestar.room.repository.RoomRepository;

@Service
@Transactional
public class RoomService {
	
	@Autowired
	private RoomRepository repository;

	//查詢所有房間
	public List<RoomVO> findAll(){
		return repository.findAll();
	}
	
	//查詢單一房間
	public RoomVO findById(Integer id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"找不到對應ID的房間"));  //找不到對應id時，回傳錯誤訊息
	}
	
	//新增或更新房間
	public RoomVO save(RoomVO room) {
		return repository.save(room);
	}
	
	//刪除房間
	public void deleteById(Integer id) {
		repository.deleteById(id);
	}
}
