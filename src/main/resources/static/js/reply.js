// 頁面載入完成後自動執行
window.addEventListener('DOMContentLoaded', () => {
    loadFeedback();
});

// 載入所有回報資料
async function loadFeedback() {
    try {
        const response = await fetch('/feedback/all');
        const data = await response.json();
        
        const tbody = document.getElementById('feedbackTableBody');
        tbody.innerHTML = ''; // 清空舊資料
        
		data.forEach(item => {
		    const isReplied = item.ticketStatus === 1;
		    const row = `<tr>
		        <td>${item.ticketId}</td>
		        <td>${item.memberId}</td>
		        <td>${item.email}</td>
		        <td>${item.subject}</td>
		        <td>${isReplied ? '已回覆' : '待處理'}</td>
		        <td>
		            ${isReplied 
		                ? '<button disabled class="btn ghost">已回覆</button>' 
		                : `<button onclick="openModal(${item.ticketId}, '${item.email}', '${item.subject}')" class="btn">回覆</button>`
		            }
		        </td>
		    </tr>`;
		    tbody.insertAdjacentHTML('beforeend', row);
		});
    } catch (error) {
        console.error('載入資料失敗:', error);
    }
}

// 開啟彈窗
function openModal(id, email, content) {
    document.getElementById('modalTicketId').value = id;
    document.getElementById('modalEmail').value = email;
    document.getElementById('modalContent').innerText = content;
    document.getElementById('replyModal').style.display = 'flex';
}

// 關閉彈窗
function closeModal() {
    document.getElementById('replyModal').style.display = 'none';
    document.getElementById('replyContent').value = '';
}

// 提交回覆
async function submitReply() {
    // 1. 取得 DOM 元素的值
    const ticketId = document.getElementById('modalTicketId').value;
    const email = document.getElementById('modalEmail').value;
    const replyContent = document.getElementById('replyContent').value;
    
    if (!replyContent.trim()) {
        alert("請輸入回覆內容！");
        return;
    }

    // --- 關鍵修改：在這裡定義 dbFormData ---
    const dbFormData = new URLSearchParams();
    dbFormData.append('ticketId', ticketId);
    dbFormData.append('replyContent', replyContent);
    dbFormData.append('employeeId', 1);
    // ------------------------------------

    // 2. 發送更新資料庫請求
    const dbRes = await fetch('/feedback/reply', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: dbFormData // 現在 dbFormData 有被定義了，不會報錯
    });

    if (!dbRes.ok) {
        alert("資料庫更新失敗。");
        return;
    }

    // 3. 寄送郵件
    const mailRes = await fetch('/feedback/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            'ticketId': ticketId,
            'email': email,
            'message': replyContent
        })
    });

    if (mailRes.ok) {
        alert("回覆成功且郵件已寄出！");
        closeModal();
        loadFeedback(); // 重新整理表格
    } else {
        alert("資料庫更新成功，但寄信失敗。");
    }
}