package com.thestar.room.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

public class RoomTypePhotoDTO {

	private Integer roomTypePhotoId;

	@NotNull(message = "房型編號不能為空")
	private Integer roomTypeId;


	private MultipartFile roomTypePic;

	
	public Integer getRoomTypePhotoId() {
		return roomTypePhotoId;
	}

	public void setRoomTypePhotoId(Integer roomTypePhotoId) {
		this.roomTypePhotoId = roomTypePhotoId;
	}

	public Integer getRoomTypeId() {
		return roomTypeId;
	}

	public void setRoomTypeId(Integer roomTypeId) {
		this.roomTypeId = roomTypeId;
	}

	public MultipartFile getRoomTypePic() {
		return roomTypePic;
	}

	public void setRoomTypePic(MultipartFile roomTypePic) {
		this.roomTypePic = roomTypePic;
	}
	
}
