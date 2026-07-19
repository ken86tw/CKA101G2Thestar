document.addEventListener('DOMContentLoaded', () => {
    console.log('Room detail page loaded.');

    const photoIdsEl = document.getElementById('photo-ids');
    if (!photoIdsEl) return;

    const ids = photoIdsEl.getAttribute('data-ids').split(',');
    let currentIndex = 0;
    const imgElement = document.getElementById('main-room-img');

    // 切換圖片的函式
    const updateImage = () => {
        const newId = ids[currentIndex];
        // 更新圖片來源
        imgElement.src = `/roomtypephoto/display/photo/${newId}`;
    };

    // 處理下一張
    const nextBtn = document.querySelector('.nav-next');
    if (nextBtn) {
        nextBtn.addEventListener('click', (e) => {
            e.preventDefault();
            currentIndex = (currentIndex + 1) % ids.length;
            updateImage();
        });
    }

    // 處理上一張
    const prevBtn = document.querySelector('.nav-prev');
    if (prevBtn) {
        prevBtn.addEventListener('click', (e) => {
            e.preventDefault();
            currentIndex = (currentIndex - 1 + ids.length) % ids.length;
            updateImage();
        });
    }
});

document.addEventListener("DOMContentLoaded", function() {
    const amenitiesElement = document.querySelector('.amenities-text');
    if (amenitiesElement) {
        const rawText = amenitiesElement.innerText;
        // 將字串依逗號切分，過濾掉空白項目，並轉成 <li> 標籤
        const items = rawText.split(',')
            .filter(item => item.trim() !== "")
            .map(item => `<li>${item.trim()}</li>`)
            .join('');

        // 將內容替換成 <ul> 清單
        amenitiesElement.innerHTML = `<ul class="amenities-list">${items}</ul>`;
    }
});