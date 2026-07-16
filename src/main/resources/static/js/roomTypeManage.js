/**
 * THE STAR Hotel - 房型管理系統互動模組 (全功能整合版 v2.3)
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log("THE STAR 系統交互已啟動");

    // 1. 初始化導覽列
    initNavigation();

    // 2. 刪除確認
    initDeleteConfirmations();
    
    // 3. 即時圖片預覽
    initImagePreview();

    // 4. 全表單驗證 (名稱、數量、價格、說明)
    initFormValidations();
});

// 導覽列切換
function initNavigation() {
    const navLinks = document.querySelectorAll('.nav-links a');
    navLinks.forEach(link => {
        link.addEventListener('click', function() {
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });
}

// 刪除確認
function initDeleteConfirmations() {
    const deleteButtons = document.querySelectorAll('.btn-icon');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (this.querySelector('.fa-trash')) {
                if (!confirm("確定刪除此項目嗎？此操作無法復原。")) {
                    e.preventDefault();
                }
            }
        });
    });
}

// 即時圖片預覽
function initImagePreview() {
    const fileInput = document.getElementById('imageInput');
    const previewImg = document.getElementById('previewImg');
    if (fileInput && previewImg) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewImg.src = e.target.result;
                    previewImg.style.display = 'block';
                    previewImg.style.opacity = 0;
                    previewImg.style.transition = "opacity 0.6s ease-in-out";
                    setTimeout(() => { previewImg.style.opacity = 1; }, 50);
                };
                reader.readAsDataURL(this.files[0]);
            }
        });
    }
}

// 統一表單驗證：名稱、數量、價格、說明
function initFormValidations() {
    const form = document.querySelector('form');
    
    // 定義驗證規則
    const fields = [
        { id: 'roomTypeName', pattern: /^[\u4e00-\u9fa5a-zA-Z0-9]+$/, errorMsg: "請輸入中文、英文或數字，且不含特殊符號" },
        { id: 'amountInput', pattern: /^[1-9][0-9]*$/, errorMsg: "請輸入大於 0 的有效整數" },
        { id: 'priceInput', pattern: /^[1-9][0-9]*$/, errorMsg: "請輸入大於 0 的金額" },
        { id: 'contentInput', pattern: /.+/, errorMsg: "房型說明不可為空" }
    ];

    fields.forEach(field => {
        const input = document.getElementById(field.id);
        if (!input) return;

        // 若尚未建立錯誤提示，則動態建立
        if (!input.nextElementSibling || !input.nextElementSibling.classList.contains('error-msg')) {
            const errorSpan = document.createElement('span');
            errorSpan.className = 'error-msg';
            errorSpan.style.color = '#a85b50';
            errorSpan.style.fontSize = '12px';
            errorSpan.style.display = 'none';
            errorSpan.innerText = field.errorMsg;
            input.parentNode.insertBefore(errorSpan, input.nextSibling);
        }

        // 輸入監聽
        input.addEventListener('input', function() {
            // 若為數字欄位，強制濾除非數字字元
            if (field.id === 'amountInput' || field.id === 'priceInput') {
                this.value = this.value.replace(/[^0-9]/g, '');
                // 額外防止輸入0
                if (this.value === '0') this.value = '';
            }

            const isValid = field.pattern.test(this.value.trim());
            const errorSpan = input.nextElementSibling;
            
            if (this.value !== "" && !isValid) {
                errorSpan.style.display = 'block';
                this.style.borderColor = '#a85b50';
            } else {
                errorSpan.style.display = 'none';
                this.style.borderColor = '#e0dcd5';
            }
        });
    });

    // 表單送出前的總防禦
    if (form) {
        form.addEventListener('submit', function(e) {
            let hasError = false;
            fields.forEach(field => {
                const input = document.getElementById(field.id);
                if (input && (!input.value.trim() || !field.pattern.test(input.value.trim()))) {
                    hasError = true;
                    input.style.borderColor = '#a85b50';
                    if (input.nextElementSibling) input.nextElementSibling.style.display = 'block';
                }
            });

            if (hasError) {
                e.preventDefault();
                alert("表單驗證未通過：請確保所有欄位已填寫正確數值，且房型說明不可為空。");
            }
        });
    }
}