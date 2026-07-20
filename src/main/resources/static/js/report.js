document.getElementById('feedbackForm').addEventListener('submit', async function(e) {
    e.preventDefault(); // 阻止表單預設送出

    // 1. 取得並鎖定按鈕（防止重複點擊）
    const submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerText = "送出中...";

    // 2. 獲取欄位資料
    const subject = document.getElementById('subject').value.trim();
    const content = document.getElementById('content').value.trim();
    const email = document.getElementById('email').value.trim();
    const memberIdValue = document.getElementById('memberId').value;
    const memberId = memberIdValue ? parseInt(memberIdValue) : null;

    // 3. 前端簡易防禦性檢查
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!subject || !content || !email) {
        alert('請填寫聯絡信箱、主旨與內容！');
        resetButton(submitBtn);
        return;
    }

    if (!emailPattern.test(email)) {
        alert('請輸入正確的電子郵件格式');
        resetButton(submitBtn);
        return;
    }

    // 4. 發送 AJAX 請求
    try {
        const response = await fetch('/feedback/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ memberId, email, subject, content })
        });

        // 5. 處理後端回應
        if (response.ok) {
            alert('感謝您的回報，我們將盡快回覆您！');
            window.location.reload(); 
        } else {
            const errorMsg = await response.text();
            
            // 如果是登入問題 (根據後端邏輯判斷)
            if (response.status === 500 && errorMsg.includes("登入")) {
                alert("登入狀態已過期，即將為您導向登入頁面。");
                window.location.href = "/login";
            } else {
                alert('送出失敗：' + (errorMsg || '請稍後再試。'));
                resetButton(submitBtn);
            }
        }
    } catch (error) {
        console.error('Error:', error);
        alert('系統連線發生錯誤，請稍後再試。');
        resetButton(submitBtn);
    }
});

// 輔助函式：恢復按鈕狀態
function resetButton(btn) {
    btn.disabled = false;
    btn.innerText = "送出";
}