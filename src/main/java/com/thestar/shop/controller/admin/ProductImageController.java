package com.thestar.shop.controller.admin;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.thestar.shop.entity.ProductImageVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.service.ProductImageService;
import com.thestar.shop.service.ProductsService;

@Controller
@RequestMapping("/admin/shop/image")
public class ProductImageController {

	private static final Logger logger = LoggerFactory.getLogger(ProductImageController.class);

	// 允許上傳的圖片類型
	private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/png", "image/jpeg", "image/jpg",
			"image/gif", "image/webp");

	// 單張圖片大小上限：5MB
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

	// 同一商品的封面設定需要序列化，避免同時上傳造成多張封面
	private final Object coverLock = new Object();

	@Autowired
	ProductImageService productImageSvc;

	@Autowired
	ProductsService productsSvc;

	// 顯示某商品的所有圖片
	@GetMapping("manage/{productId}")
	public String manageImages(@PathVariable Integer productId, ModelMap model) {
		ProductsVO productsVO = productsSvc.getOneProduct(productId);
		List<ProductImageVO> imageList = productImageSvc.getByProductId(productId);

		ProductImageVO coverImage = productImageSvc.getFirstImage(productId);

		// 封面放第一張
		// 注意：不能直接用 imageList.remove(coverImage)，
		// 因為 coverImage 與 imageList 中的物件是不同次查詢取得的實例，
		// 若 ProductImageVO 沒有覆寫 equals()/hashCode()，remove() 會比對失敗（用記憶體位址比較），
		// 導致同一張圖片同時出現在移除前與加入後的位置，造成畫面上重複顯示。
		// 因此改用 productImageId 比對來移除對應項目。
		if (coverImage != null) {
			imageList.removeIf(img -> img.getProductImageId().equals(coverImage.getProductImageId()));
			imageList.add(0, coverImage);
		}
		model.addAttribute("productsVO", productsVO);
		model.addAttribute("imageListData", imageList);
		return "admin/shop/image/manageImages";
	}

	// 上傳圖片
	@PostMapping("upload")
	public String uploadImage(@RequestParam("productId") Integer productId,
			@RequestParam("productImage") MultipartFile[] parts, ModelMap model) throws IOException {

		synchronized (coverLock) {
			boolean hasImage = productImageSvc.hasImage(productId);

			for (MultipartFile file : parts) {

				if (file.isEmpty()) {
					continue;
				}

				// 檔案大小檢查
				if (file.getSize() > MAX_FILE_SIZE) {
					logger.warn("圖片超過大小限制，忽略此檔案：{}，大小={} bytes", file.getOriginalFilename(), file.getSize());
					continue;
				}

				// 檔案類型檢查
				String contentType = file.getContentType();
				if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
					logger.warn("不支援的檔案類型，忽略此檔案：{}，contentType={}", file.getOriginalFilename(), contentType);
					continue;
				}

				ProductImageVO productImageVO = new ProductImageVO();

				productImageVO.setProductId(productId);
				productImageVO.setProductImage(file.getBytes());

				// 商品第一張圖片設定為封面
				if (!hasImage) {
					productImageVO.setIsCover((byte) 1);
					hasImage = true;
				} else {
					productImageVO.setIsCover((byte) 0);
				}

				productImageSvc.addProductImage(productImageVO);

				logger.debug("圖片存入，商品ID={}, 封面={}", productImageVO.getProductId(), productImageVO.getIsCover());
			}
		}

		return "redirect:/admin/shop/image/manage/" + productId;
	}

	// 刪除圖片
	// 注意：封面重新指定的邏輯已經在 ProductImageService.deleteProductImage 內處理
	// （刪除封面圖後，會透過 repository.updateCover 自動指定新的封面），
	// Controller 不需要重複處理，直接呼叫 Service 即可。
	@PostMapping("delete")
	public String deleteImage(@RequestParam("productImageId") Integer productImageId,
			@RequestParam("productId") Integer productId) {

		productImageSvc.deleteProductImage(productImageId);

		return "redirect:/admin/shop/image/manage/" + productId;
	}

	// 後台顯示圖片
	@GetMapping(value = "show/{productImageId}")
	@ResponseBody
	public ResponseEntity<byte[]> showImage(@PathVariable Integer productImageId) {
		ProductImageVO img = productImageSvc.getOneProductImage(productImageId);
		if (img != null && img.getProductImage() != null) {
			HttpHeaders headers = new HttpHeaders();
			byte[] data = img.getProductImage();
			headers.setContentType(detectImageMediaType(data));
			return new ResponseEntity<>(data, headers, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// 依 magic bytes 判斷圖片格式，涵蓋 PNG / JPEG / GIF / WebP，其餘預設為 JPEG
	private MediaType detectImageMediaType(byte[] data) {
		if (data.length > 8 && data[0] == (byte) 0x89 && data[1] == (byte) 0x50 && data[2] == (byte) 0x4E
				&& data[3] == (byte) 0x47) {
			return MediaType.IMAGE_PNG;
		}
		if (data.length > 3 && data[0] == (byte) 0x47 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46) {
			return MediaType.valueOf("image/gif");
		}
		if (data.length > 12 && data[0] == (byte) 0x52 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46
				&& data[3] == (byte) 0x46 && data[8] == (byte) 0x57 && data[9] == (byte) 0x45 && data[10] == (byte) 0x42
				&& data[11] == (byte) 0x50) {
			return MediaType.valueOf("image/webp");
		}
		// 預設當作 JPEG（包含真正的 JPEG 檔）
		return MediaType.IMAGE_JPEG;
	}
}