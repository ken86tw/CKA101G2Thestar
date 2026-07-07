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

import com.thestar.shop.entity.ProductImageVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.service.ProductCategoryService;
import com.thestar.shop.service.ProductImageService;
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

	// 顯示所有商品
	@GetMapping
	public String listAllProducts(@RequestParam(value = "categoryId", required = false) Integer categoryId,
			ModelMap model) {

		List<ProductsVO> list;
		if (categoryId != null) {
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
		model.addAttribute("firstImageMap", firstImageMap);
		return "user/shop/listAllProducts";
	}

	// 顯示單筆商品
	@GetMapping("/{productId}")
	public String listOneProduct(
	        @PathVariable Integer productId,
	        ModelMap model) {

	    ProductsVO productsVO =
	            productsSvc.getOneProduct(productId);


	    // 全部圖片
	    List<ProductImageVO> imageList =
	            productImageSvc.getByProductId(productId);


	    // 封面圖片
	    ProductImageVO coverImage =
	            productImageSvc.getFirstImage(productId);


	    model.addAttribute("productsVO", productsVO);

	    model.addAttribute("imageListData", imageList);

	    model.addAttribute("coverImage", coverImage);


	    return "user/shop/listOneProduct";
	}

	// 顯示圖片
	@GetMapping(value = "image/{productImageId}")
	@ResponseBody
	public ResponseEntity<byte[]> showImage(@PathVariable Integer productImageId) {
		ProductImageVO img = productImageSvc.getOneProductImage(productImageId);
		if (img != null && img.getProductImage() != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_JPEG);
			// 嘗試判斷是否為 PNG
			byte[] data = img.getProductImage();
			if (data.length > 4 && data[0] == (byte) 0x89 && data[1] == (byte) 0x50) {
				headers.setContentType(MediaType.IMAGE_PNG);
			}
			return new ResponseEntity<>(data, headers, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}