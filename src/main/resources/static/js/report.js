document.getElementById('feedbackForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const feedbackData = {
        memberId: document.getElementById('memberId').value,
        email: document.getElementById('email').value,
        subject: document.getElementById('subject').value,
        content: document.getElementById('content').value
    };

    // 修正路徑：將 '/feedback/api/add' 改為 '/feedback/add'
    fetch('/feedback/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(feedbackData)
    })
    .then(response => {
        if (response.ok) {
            alert('感謝您的回報，我們將盡快處理！');
            window.location.reload();
        } else {
            alert('送出失敗，請稍後再試。');
        }
    })
    .catch(error => console.error('Error:', error));
});