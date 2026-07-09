package com.thestar.shop.controller.admin;

import com.thestar.shop.entity.ProductCategoryVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.service.ProductCategoryService;
import com.thestar.shop.service.ProductsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/shop")
public class ProductsController {

    @Autowired
    ProductsService productsSvc;

    @Autowired
    ProductCategoryService productCategorySvc;

    // 顯示新增頁面
    @GetMapping("addProduct")
    public String addProduct(ModelMap model) {
        model.addAttribute("productsVO", new ProductsVO());
        model.addAttribute("categoryListData", productCategorySvc.getAll());
        return "admin/shop/addProduct";
    }

    // 執行新增
    @PostMapping("insert")
    public String insert(@Valid ProductsVO productsVO, BindingResult result, ModelMap model) {
        if (result.hasErrors()) {
            model.addAttribute("categoryListData", productCategorySvc.getAll());
            return "admin/shop/addProduct";
        }
        productsSvc.addProduct(productsVO);
        return "redirect:/admin/shop/listAllProducts";
    }

    // 顯示所有商品
    @GetMapping("listAllProducts")
    public String listAllProducts(ModelMap model) {
        List<ProductsVO> list = productsSvc.getAll();
        model.addAttribute("productsListData", list);
        return "admin/shop/listAllProducts";
    }

    // 準備修改
    @PostMapping("getOne_For_Update")
    public String getOne_For_Update(@RequestParam("productId") Integer productId, ModelMap model) {
        ProductsVO productsVO = productsSvc.getOneProduct(productId);
        model.addAttribute("productsVO", productsVO);
        model.addAttribute("categoryListData", productCategorySvc.getAll());
        return "admin/shop/update_product_input";
    }

    // 執行修改
    @PostMapping("update")
    public String update(@Valid ProductsVO productsVO, BindingResult result, ModelMap model) {
        if (result.hasErrors()) {
            model.addAttribute("categoryListData", productCategorySvc.getAll());
            return "admin/shop/update_product_input";
        }
        productsSvc.updateProduct(productsVO);
        model.addAttribute("productsVO", productsVO);
        return "admin/shop/listOneProduct";
    }

    // 執行刪除
    @PostMapping("delete")
    public String delete(@RequestParam("productId") Integer productId) {
        productsSvc.deleteProduct(productId);
        return "redirect:/admin/shop/listAllProducts";
    }
}