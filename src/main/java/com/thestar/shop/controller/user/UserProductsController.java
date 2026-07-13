package com.thestar.shop.controller.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.thestar.shop.entity.ProductCategoryVO;
import com.thestar.shop.entity.ProductImageVO;
import com.thestar.shop.entity.ProductReviewVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.service.ProductCategoryService;
import com.thestar.shop.service.ProductImageService;
import com.thestar.shop.service.ProductReviewService;
import com.thestar.shop.service.ProductsService;

@Controller
@RequestMapping("/shop")
public class UserProductsController {

	@Autowired
	ProductsService productsSvc;

	@Autowired
	ProductCategoryService productCategorySvc;

	@Autowired
	ProductImageService productImageSvc;

	@Autowired
	ProductReviewService productReviewSvc;

	// 顯示所有商品
	@GetMapping
	public String listAllProducts(
			@RequestParam(value = "categoryId", required = false) Integer categoryId,
			@RequestParam(value = "keyword", required = false) String keyword,
			ModelMap model) {

		List<ProductsVO> list;

		if (keyword != null && !keyword.trim().isEmpty()) {
			// 關鍵字搜尋（忽略類別篩選）
			list = productsSvc.searchByKeyword(keyword.trim());
		} else if (categoryId != null) {
			list = productsSvc.getAllByStatusAndCategory((byte) 1, categoryId);
		} else {
			list = productsSvc.getAllByStatus((byte) 1);
		}

		// 每個商品取封面圖片
		Map<Integer, ProductImageVO> firstImageMap = new HashMap<>();
		for (ProductsVO product : list) {
			ProductImageVO image = productImageSvc.getFirstImage(product.getProductId());
			if (image != null) {
				firstImageMap.put(product.getProductId(), image);
			}
		}

		model.addAttribute("productsListData", list);
		model.addAttribute("categoryListData", productCategorySvc.getAll());
		model.addAttribute("selectedCategoryId", categoryId);
		model.addAttribute("keyword", keyword);
		model.addAttribute("firstImageMap", firstImageMap);
		return "user/shop/listAllProducts";
	}

	// 顯示單筆商品
	@GetMapping("/{productId}")
	public String listOneProduct(@PathVariable Integer productId, ModelMap model) {

		ProductsVO productsVO = productsSvc.getOneProduct(productId);

		// 取得類別名稱
		ProductCategoryVO categoryVO = productCategorySvc.getOneProductCategory(productsVO.getProductCategoryId());
		String categoryName = (categoryVO != null) ? categoryVO.getProductCategoryName() : "飯店精選商品";

		// 全部圖片
		List<ProductImageVO> imageList = productImageSvc.getByProductId(productId);

		// 封面圖片
		ProductImageVO coverImage = productImageSvc.getFirstImage(productId);

		// 評論列表
		List<ProductReviewVO> reviewList = productReviewSvc.getByProductId(productId);

		model.addAttribute("productsVO", productsVO);
		model.addAttribute("categoryName", categoryName);
		model.addAttribute("imageListData", imageList);
		model.addAttribute("coverImage", coverImage);
		model.addAttribute("reviewListData", reviewList);
		return "user/shop/listOneProduct";
	}

	// 顯示圖片（支援 PNG / JPEG / GIF / WebP）
	@GetMapping(value = "image/{productImageId}")
	@ResponseBody
	public ResponseEntity<byte[]> showImage(@PathVariable Integer productImageId) {
		ProductImageVO img = productImageSvc.getOneProductImage(productImageId);
		if (img != null && img.getProductImage() != null) {
			byte[] data = img.getProductImage();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(detectImageMediaType(data));
			return new ResponseEntity<>(data, headers, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// 判斷圖片格式（與後台 ProductImageController 相同邏輯）
	private MediaType detectImageMediaType(byte[] data) {
		if (data.length > 8
				&& data[0] == (byte) 0x89 && data[1] == (byte) 0x50
				&& data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
			return MediaType.IMAGE_PNG;
		}
		if (data.length > 3
				&& data[0] == (byte) 0x47 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46) {
			return MediaType.valueOf("image/gif");
		}
		if (data.length > 12
				&& data[0] == (byte) 0x52 && data[1] == (byte) 0x49
				&& data[2] == (byte) 0x46 && data[3] == (byte) 0x46
				&& data[8] == (byte) 0x57 && data[9] == (byte) 0x45
				&& data[10] == (byte) 0x42 && data[11] == (byte) 0x50) {
			return MediaType.valueOf("image/webp");
		}
		return MediaType.IMAGE_JPEG;
	}
}