package com.thestar.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.thestar.room.entity.RoomTypePhotoVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.repository.RoomTypePhotoRepository;
import com.thestar.room.repository.RoomTypeRepository;

@Component
public class RoomTypePhotoInitializer implements CommandLineRunner {

	@Autowired
	private RoomTypePhotoRepository photoRepo;

	@Autowired
	private RoomTypeRepository roomTypeRepo;

	@Override
	public void run(String... args) throws Exception {
		String[] roomIds = { "1", "2", "3" };
		// 圖片根目錄
		Path rootPath = Paths.get("src/main/resources/static/images/room/");

		for (String id : roomIds) {
			Integer roomId = Integer.parseInt(id);
			RoomTypeVO roomType = roomTypeRepo.findById(roomId).orElse(null);
			if (roomType == null)
				continue;

			List<RoomTypePhotoVO> existingPhotos = photoRepo.findByRoomTypeVORoomTypeId(roomId);

			int savedCount = 0;
			int foundCount = 0;

			if (Files.exists(rootPath) && Files.isDirectory(rootPath)) {
				try (Stream<Path> stream = Files.list(rootPath)) {
					// 關鍵修改：篩選規則改為檔名開頭必須是 "房型ID-"
					List<Path> imageFiles = stream.filter(
							p -> p.toString().endsWith(".jpg") && p.getFileName().toString().startsWith(id + "-"))
							.collect(Collectors.toList());
					foundCount = imageFiles.size();

					for (Path filePath : imageFiles) {
						byte[] imageBytes = Files.readAllBytes(filePath);

						// 檢查是否已存在
						if (existingPhotos.stream().anyMatch(
								p -> p.getRoomTypePic() != null && p.getRoomTypePic().length == imageBytes.length)) {
							continue;
						}

						// 儲存邏輯
						RoomTypePhotoVO photo = existingPhotos.stream()
								.filter(p -> p.getRoomTypePic() == null || p.getRoomTypePic().length == 0).findFirst()
								.orElse(new RoomTypePhotoVO());

						photo.setRoomTypeVO(roomType);
						photo.setRoomTypePic(imageBytes);
						photoRepo.save(photo);
						existingPhotos.add(photo);
						savedCount++;
					}
				}
			}

			// 輸出邏輯
			if (savedCount > 0) {
				System.out.println("房型 " + id + " 成功同步 " + savedCount + " 張圖片。");
			} else if (foundCount > 0) {
				System.out.println("房型 " + id + " 的圖片均已存在，無需更新。");
			}
		}
	}
}