document.addEventListener("DOMContentLoaded", function() {
    
    // --- 1. 成功提示訊息自動消失功能 ---
    const successMessageDiv = document.getElementById('successMessageDiv');
    if (successMessageDiv) {
        setTimeout(function() {
            successMessageDiv.style.transition = "opacity 0.5s ease";
            successMessageDiv.style.opacity = "0";
            setTimeout(function() {
                successMessageDiv.style.display = 'none';
            }, 500);
        }, 3000);
    }

    // --- 2. 圖片處理邏輯 ---
    const imageInput = document.getElementById('imageFilesInput'); 
    const uploadBtn = document.getElementById('uploadBtn');
    const previewContainer = document.getElementById('previewContainer');
    let dataTransfer = new DataTransfer();

    if (uploadBtn && imageInput) {
        uploadBtn.addEventListener('click', function() {
            imageInput.click();
        });
    }

    if (imageInput && previewContainer) {
        imageInput.addEventListener('change', function(event) {
            const files = event.target.files;
            const MAX_SIZE = 5 * 1024 * 1024;
            
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                if (!file.type.startsWith('image/')) continue;
                if (file.size > MAX_SIZE) {
                    alert(`檔案 ${file.name} 太大，請選擇 5MB 以下的圖片`);
                    continue;
                }
                dataTransfer.items.add(file);
                const reader = new FileReader();
                reader.onload = function(e) {
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.className = 'preview-img'; 
                    previewContainer.appendChild(img);
                };
                reader.readAsDataURL(file);
            }
            imageInput.files = dataTransfer.files;
        });
    }

    // --- 3. 核心驗證邏輯 ---
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(event) {
            // 這裡保留您原本的庫存邏輯即可
            const amountInput = document.getElementById('amountInput'); // 若您頁面有此 ID
            if (amountInput) {
                const enteredValue = parseInt(amountInput.value);
                const minAllowed = parseInt(amountInput.getAttribute('min')) || 1;
                if (enteredValue < minAllowed) {
                    event.preventDefault();
                    alert(`庫存數量不可小於系統中已建立的房間總數 (${minAllowed} 間)。`);
                    return;
                }
            }
        });
    }

    // --- 4. 刪除圖片按鈕確認 ---
    const deleteButtons = document.querySelectorAll('.delete-btn');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (!confirm('確定要刪除這張圖片嗎？')) {
                e.preventDefault();
            }
        });
    });
});