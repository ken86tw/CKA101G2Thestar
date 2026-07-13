/**
 * THE STAR Hotel - 房型管理系統互動模組
 * 專為飯店後台設計，強調流暢性與簡約美感
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log("THE STAR 系統交互已啟動 (v2.0)");

    // 1. 初始化導覽列與標籤狀態
    initNavigation();

    // 2. 優雅的刪除處理 (加入了 CSS 動畫延遲，體驗更平滑)
    initDeleteConfirmations();
    
    // 3. 即時圖片預覽 (結合透明度過渡，符合圖片細膩風格)
    initImagePreview();
});

// 優化導覽列切換：加入 CSS 類別變更，讓樣式即時響應
function initNavigation() {
    const navLinks = document.querySelectorAll('.nav-links a');
    navLinks.forEach(link => {
        link.addEventListener('click', function() {
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });
}

// 刪除確認：飯店後台不需要過多干擾，使用簡潔的確認機制
function initDeleteConfirmations() {
    // 這裡對應我們 HTML 中的按鈕類別
    const deleteButtons = document.querySelectorAll('.btn-del, .btn-icon');
    
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            // 若按鈕包含刪除圖示或相關 Class，則攔截並確認
            if (this.querySelector('.fa-trash') || this.classList.contains('btn-del')) {
                if (!confirm("確認執行此刪除操作？")) {
                    e.preventDefault();
                }
            }
        });
    });
}

// 即時圖片預覽：配合 THE STAR 的簡潔預覽區域
function initImagePreview() {
    const fileInput = document.querySelector('input[type="file"]');
    const previewImg = document.querySelector('#previewImg'); // 建議在 HTML 表單給圖片 ID

    if (fileInput && previewImg) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewImg.style.opacity = 0; // 隱藏後淡入
                    previewImg.src = e.target.result;
                    
                    // 利用 CSS Transition 產生飯店等級的優雅過渡
                    previewImg.style.transition = "opacity 0.6s ease-in-out";
                    setTimeout(() => { previewImg.style.opacity = 1; }, 50);
                };
                reader.readAsDataURL(this.files[0]);
            }
        });
    }
}