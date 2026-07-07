package com.thestar.shop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PRODUCT_IMAGE")
public class ProductImageVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PRODUCT_IMAGE_ID")
	private Integer productImageId;

	@Column(name = "PRODUCT_ID", nullable = false)
	private Integer productId;

	@Column(name = "PRODUCT_IMAGE", columnDefinition = "LONGBLOB")
	private byte[] productImage;

	@Column(name = "IS_COVER")
	private Byte isCover = 0;

	@ManyToOne
	@JoinColumn(name = "PRODUCT_ID", insertable = false, updatable = false)
	private ProductsVO product;

	public Integer getProductImageId() {
		return productImageId;
	}

	public void setProductImageId(Integer productImageId) {
		this.productImageId = productImageId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public byte[] getProductImage() {
		return productImage;
	}

	public void setProductImage(byte[] productImage) {
		this.productImage = productImage;
	}

	public ProductsVO getProduct() {
		return product;
	}

	public void setProduct(ProductsVO product) {
		this.product = product;
	}

	public Byte getIsCover() {
		return isCover;
	}

	public void setIsCover(Byte isCover) {
		this.isCover = isCover;
	}
}