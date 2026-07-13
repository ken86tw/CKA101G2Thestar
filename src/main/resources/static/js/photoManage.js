document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('photoInput');
    const previewImg = document.getElementById('previewImage');
    
    // 獲取 HTML 中定義的兩個隱藏欄位 (請務必檢查 HTML 中的 ID 設定)
    const photoIdField = document.getElementById('roomTypePhotoId');
    const roomIdField = document.getElementById('roomTypeId');
    
    const photoId = photoIdField ? photoIdField.value : "";
    const roomId = roomIdField ? roomIdField.value : "";

    console.log("偵測到 - PhotoId:", photoId, "RoomId:", roomId);

    // 邏輯判斷：
    // 1. 如果有 photoId，代表在編輯特定圖片 -> 載入特定圖片
    // 2. 如果沒有 photoId 但有 roomId，代表在管理頁面 -> 載入該房型首圖
    let imageUrl = "";
    if (photoId && photoId !== "") {
        imageUrl = "/roomtypephoto/display/photo/" + photoId;
    } else if (roomId && roomId !== "") {
        imageUrl = "/roomtypephoto/display/room/" + roomId;
    }

    // 若有決定好要載入哪張圖，則設定來源
    if (imageUrl !== "") {
        console.log("正在載入預覽圖:", imageUrl);
        previewImg.src = imageUrl;
        previewImg.style.display = 'block';
        
        previewImg.onerror = function() {
            console.error("圖片載入失敗，可能該房型無圖片或路徑錯誤");
            // 可選：載入失敗時隱藏圖片或顯示預設圖
        };
    }

    // 監聽上傳事件 (使用者手動選擇新檔案時)
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(event) {
                    previewImg.src = event.target.result;
                    previewImg.style.display = 'block';
                };
                reader.readAsDataURL(file);
            }
        });
    }
});