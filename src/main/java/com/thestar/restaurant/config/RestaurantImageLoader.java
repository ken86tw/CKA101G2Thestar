package com.thestar.restaurant.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Component
public class RestaurantImageLoader implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        // 圖片存放的根目錄
        String photosRoot = "src/main/resources/static/images/restaurant";
        
        // 方案 A：使用 UPDATE 語法更新現有餐點的圖片
        String updateSql = "UPDATE RESTAURANT_MENU SET ITEM_IMAGE = ? WHERE ITEM_ID = ?";

        File rootDir = new File(photosRoot);
        if (!rootDir.exists()) {
            System.out.println("餐廳圖片資料夾不存在，跳過載入。");
            return;
        }

        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(updateSql)) {

            // 依序檢查 1.jpg 到 5.jpg
            for (int i = 1; i <= 5; i++) {
                File imageFile = new File(rootDir, i + ".jpg");

                if (imageFile.exists()) {
                    try (InputStream fin = new FileInputStream(imageFile)) {
                        // 填入參數
                        pstmt.setBinaryStream(1, fin);
                        pstmt.setInt(2, i); // 對應 ITEM_ID = 1, 2, 3, 4, 5

                        int rowsUpdated = pstmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            System.out.println("已成功載入餐點 ID " + i + " 的圖片：" + imageFile.getName());
                        } else {
                            System.out.println("警告：資料庫中找不到餐點 ID " + i + "，無法更新圖片。");
                        }
                    }
                } else {
                    System.out.println("未找到圖片：" + imageFile.getName() + "，跳過此筆。");
                }
            }
            System.out.println("餐廳餐點圖片更新程序執行完畢！");
        }
    }
}