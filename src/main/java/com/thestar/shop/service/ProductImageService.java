package com.thestar.shop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.shop.entity.ProductImageVO;
import com.thestar.shop.repository.ProductImageRepository;

@Service
public class ProductImageService {

	@Autowired
	ProductImageRepository repository;

	@Transactional
	public void addProductImage(ProductImageVO productImageVO) {
		repository.saveAndFlush(productImageVO);
	}

	@Transactional
	public void deleteProductImage(Integer productImageId) {

		ProductImageVO image = getOneProductImage(productImageId);

		if (image == null) {
			return;
		}

		Integer productId = image.getProductId();

		boolean deleteCover = image.getIsCover() != null && image.getIsCover() == 1;

		repository.deleteById(productImageId);

		repository.flush();

		if (deleteCover) {

			List<ProductImageVO> images = repository.findByProductId(productId);

			if (!images.isEmpty()) {

			    ProductImageVO newCover = images.get(0);

			    repository.updateCover(
			        newCover.getProductImageId()
			    );
			}
		}
	}

	public ProductImageVO getOneProductImage(Integer productImageId) {
		Optional<ProductImageVO> optional = repository.findById(productImageId);
		return optional.orElse(null);
	}

	public List<ProductImageVO> getAll() {
		return repository.findAll();
	}

	public List<ProductImageVO> getByProductId(Integer productId) {
		return repository.findByProductId(productId);
	}

	public boolean hasImage(Integer productId) {
		return !repository.findByProductId(productId).isEmpty();
	}

	public ProductImageVO getFirstImage(Integer productId) {

		ProductImageVO image = repository.findFirstByProductIdAndIsCover(productId, (byte) 1);

		return image;
	}
	
	@Transactional
	public void setCover(Integer productImageId, Integer productId) {
	    repository.clearCoverByProductId(productId);
	    repository.updateCover(productImageId);
	}
}