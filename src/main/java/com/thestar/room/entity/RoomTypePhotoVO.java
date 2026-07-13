package com.thestar.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ROOM_TYPE_PHOTO")
public class RoomTypePhotoVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ROOM_TYPE_PHOTO_ID")
	private Integer roomTypePhotoId;

	@ManyToOne  //FK，多個房型圖片對應到一種房型
	@JoinColumn(name = "ROOM_TYPE_ID")
	private RoomTypeVO roomTypeVO;

	@Lob
	@Column(name = "ROOM_TYPE_PIC", columnDefinition = "LONGBLOB")
	private byte[] roomTypePic;

	public RoomTypePhotoVO() {
		super();
	}

	public Integer getRoomTypePhotoId() {
		return roomTypePhotoId;
	}

	public void setRoomTypePhotoId(Integer roomTypePhotoId) {
		this.roomTypePhotoId = roomTypePhotoId;
	}

	public RoomTypeVO getRoomTypeVO() {
		if (this.roomTypeVO == null) {
	        this.roomTypeVO = new RoomTypeVO(); // 若為 null 則自動初始化
	    }
		return roomTypeVO;
	}

	public void setRoomTypeVO(RoomTypeVO roomTypeVO) {
		this.roomTypeVO = roomTypeVO;
	}

	public byte[] getRoomTypePic() {
		return roomTypePic;
	}

	public void setRoomTypePic(byte[] roomTypePic) {
		this.roomTypePic = roomTypePic;
	}

}
