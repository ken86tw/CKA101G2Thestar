/* ============================================================
   roombooking/booking.js — 「預訂客房」訂房流程
   內容:flatpickr 日期日曆、查空房、選房、優惠券、
        建立訂單、付款(綠界)、確認頁倒數
   對應畫面:templates/roombooking/booking.html
   ============================================================ */
window.RB = window.RB || {};

RB.booking = {
    computed: {
        // 確認頁的剩餘秒數:不用開錶關錶 confirmOrder有單就自動有值 沒單就是null
        // nowTick每秒變一次 這裡就自動跟著重算 倒數效果就出來了
        payLeft() {
            return this.confirmOrder ? this.payLeftOf(this.confirmOrder) : null;
        },
        payLeftText() {
            return this.mmss(this.payLeft);
        },
        bookItems() {
            return this.book.results
                .filter(r => (this.book.sel[r.roomTypeId] || 0) > 0)
                .map(r => {
                    const qty = this.book.sel[r.roomTypeId];
                    return {
                        roomTypeId: r.roomTypeId, roomTypeName: r.roomTypeName,
                        price: r.price, qty, subtotal: r.price * this.book.nights * qty
                    };
                });
        },

        bookTotal() {
            return this.bookItems.reduce((sum, it) => sum + it.subtotal, 0);
        },
        couponDiscount() {
            return this.resolveCouponDiscount(this.form.memberCouponId, this.bookTotal);
        },
        // 目前選中的券物件(給預覽卡顯示用)
        selectedCoupon() {
            if (this.form.memberCouponId == null) {
                return null;
            }
            return this.coupons.find(c => Number(c.memberCouponId) === Number(this.form.memberCouponId) && c.displayStatus === 'AVAILABLE'
            ) || null;
        },
        // 「搜尋列現在有沒有顯示在畫面上?」——把 HTML 那兩層 v-if 的條件照抄過來合成一個布林值。
        // 為什麼需要它:日期欄那個 input 被 v-if 包著,v-if 是「整個元素從頁面拆掉/重建」,
        // 拆掉時掛在上面的 flatpickr 日曆也跟著沒了;所以要有個訊號告訴我們「input 回來了,日曆要重掛」
        searchbarOn() {
            // !restoring:還原訂單期間搜尋列被遮罩擋住沒渲染,等還原結束(旗子放下)
            // 這個值才翻成 true,watch 就會補掛 flatpickr 日曆
            return (this.member.on || this.browsing) && this.nav === 'book'
                && !this.confirmOrder && this.book.step === 'search' && !this.restoring;
        },
    },
    methods: {
        // 把 flatpickr 日曆掛到搜尋列的日期欄上(首次進頁面和每次搜尋列重新出現都會呼叫)
        initDatePicker() {
            const el = this.$refs.dateRange;
            if (!el) return;   // 員工身分或不在搜尋頁時,日期欄根本沒渲染,直接跳過
            // 舊實例先銷毀再重建:v-if 把舊 input 拆掉後,舊日曆還掛在記憶體裡,不清會愈積愈多
            if (this._fp) this._fp.destroy();
            // 存成 this._fp 而不是放進 data():data 裡的東西 Vue 都會加上「變動偵測」包裝,
            // 日曆物件內部狀態一大堆,被包裝會又慢又容易出怪事;它跟畫面渲染無關,存普通屬性就好
            this._fp = flatpickr(el, {
                mode: 'range',        // 範圍模式:點兩下選「起、迄」,中間的日期自動反白
                showMonths: 2,        // 一次並排顯示兩個月(Booking.com 樣式)
                minDate: 'today',     // 今天以前的日期變灰不可點(跟 searchRooms 的檢查同一條規則,這裡是第一道防線)
                locale: 'zh_tw',      // 用 <head> 載入的繁中語系:週日/週一、「2026-07-19 至 2026-07-20」
                dateFormat: 'Y-m-d',  // 跟後端 API 要的 yyyy-MM-dd 一致,不用再轉格式
                // 重新進搜尋頁時,把 form 裡現有的日期畫回日曆上(例如從確認頁按「更改日期」回來)
                defaultDate: [this.form.checkInDate, this.form.checkOutDate],
                // 使用者選完日期,flatpickr 呼叫這個回呼——這裡就是「日曆 → Vue 表單」的橋:
                // dates 是選到的日期陣列,只點了第一下時長度是 1,兩個都選好才是 2
                onChange: (dates, _str, fp) => {
                    if (dates.length === 2) {
                        // 用 fp.formatDate 轉回 yyyy-MM-dd 字串;不能用 toISOString,
                        // 理由跟 common.js 最上面 localDate 的註解一樣:那是 UTC,台灣半夜會差一天
                        this.form.checkInDate = fp.formatDate(dates[0], 'Y-m-d');
                        this.form.checkOutDate = fp.formatDate(dates[1], 'Y-m-d');
                    }
                }
            });
        },
        // 搜尋列顯示用:把 form 裡的 '2026-07-22' 轉成 '2026年7月22日(三)'。
        // 只轉「給人看的那份」,form 裡仍是 yyyy-MM-dd——後端 API 吃的是那個格式,不能動
        dateZh(s) {
            if (!s) return '';
            const [y, m, d] = s.split('-');
            // 星期幾:getDay() 回 0~6(0=週日),拿去字串裡「按位置取字」剛好對上中文
            // 注意要用 new Date(年,月,日) 三個參數的寫法(月要減1,因為它從0起算):
            // 這樣是「本地時間」的那天;寫 new Date('2026-07-22') 會被當成 UTC,某些時區星期會差一天
            const w = '日一二三四五六'[new Date(y, m - 1, d).getDay()];
            // Number() 順手把 '07' 的開頭 0 拿掉:7月 比 07月 順眼
            return `${y}年${Number(m)}月${Number(d)}日（${w}）`;
        },
        // 用「今天住到明天」查一次空房,拿到目前資料庫裡全部房型的名稱與價格
        async loadRoomTypes() {
            try {
                const r = await this.api(`/find/room?checkInDate=${localDate()}&checkOutDate=${localDate(1)}`);
                this.roomTypes = r.map(x => ({
                    id: x.roomTypeId,
                    name: x.roomTypeName || x.roomTypename,
                    price: x.price || 0
                }));
            } catch {
                this.roomTypes = [];
            }
        },

        async searchRooms() {
            const {checkInDate, checkOutDate} = this.form;
            if (!checkInDate || !checkOutDate) {
                this.toast('warn', '請選擇日期')
                return;
            }
            if (checkOutDate <= checkInDate) {
                this.toast('warn', '退房日必須晚於入住日');
                return;
            }
            if (checkInDate < localDate()) {
                this.toast('warn', '入住日不能早於今天');
                return;
            }
            try {
                const r = await this.api(`/find/room?checkInDate=${checkInDate}&checkOutDate=${checkOutDate}`)
                this.book.results = r.map(x => ({
                    roomTypeId: x.roomTypeId,
                    roomTypeName: x.roomTypeName || x.roomTypename,
                    remaining: x.amount,
                    price: x.price || 0
                }));

                this.book.nights = (new Date(checkOutDate) - new Date(checkInDate)) / 864e5;
                this.book.sel = {};
                this.book.results.forEach(x => this.book.sel[x.roomTypeId] = 0);
                this.book.step = 'list';
            } catch (e) {
                this.toast('err', '查詢失敗', this.errMsg(e))
            }
        },

        qtyOptions(r) {
            const max = Math.min(r.remaining, 10);
            const arr = [];
            for (let n = 0; n <= max; n++) arr.push(n);
            return arr;
        },

        async goConfirm() {
            if (this.bookItems.length === 0) {
                this.toast('warn', '請至少選擇一間客房');
                return;
            }
            this.form.rooms = this.bookItems.map(it => ({roomTypeId: it.roomTypeId, qty: it.qty}));
            // 未登入:在「現在就預訂」就先去登入,回來由mounted還原並直接進確認頁
            if (!this.member.on) {
                sessionStorage.setItem('pendingBooking', JSON.stringify({
                    checkInDate: this.form.checkInDate,
                    checkOutDate: this.form.checkOutDate,
                    sel: this.book.sel,
                }));
                this.toast('warn', '請先登入', '登入後將直接進入訂單確認');
                setTimeout(() => location.href = '/login.html?redirect=/roombooking.html', 800);
                return;
            }
            // 改用 await(原本是 .then):流程一樣,但整個函式變成「跑完才結束」——
            // mounted 的還原流程就能 await goConfirm(),等確認頁真的切過去才把還原遮罩放下
            const success = await this.loadCoupons();
            if (success) {
                this.book.step = 'confirm';
                // 進確認頁時重驗先前選的券:券被用掉/過期,或這次金額未達門檻就自動取消
                if (this.form.memberCouponId != null) {
                    const selectedCoupon = this.selectedCoupon;
                    if (
                        !selectedCoupon ||
                        selectedCoupon.displayStatus !== 'AVAILABLE' ||
                        !this.couponCanUse(selectedCoupon)
                    ) {
                        this.form.memberCouponId = null;

                        this.toast(
                            'warn',
                            '優惠券已取消',
                            '目前訂單金額未達消費門檻，請重新選擇優惠券'
                        );
                    }
                }
            }
        },

        async createOrder() {
            // 未登入:先把訂房內容存進sessionStorage再去登入,登入回來由mounted還原
            if (!this.member.on) {
                sessionStorage.setItem('pendingBooking', JSON.stringify({
                    checkInDate: this.form.checkInDate,
                    checkOutDate: this.form.checkOutDate,
                    sel: this.book.sel,
                }));
                this.toast('warn', '請先登入', '登入後將自動回到您的訂單');
                setTimeout(() => location.href = '/login.html?redirect=/roombooking.html', 800);
                return;
            }
            try {
                const r = await this.api('/thestar/order/create', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(this.form)
                });
                this.log('✔ 下單', r);
                this.confirmOrder = r;   // 設好confirmOrder後 computed的payLeft就會自動開始倒數
                this.book.step = 'search';
                this.book.results = [];
                this.book.sel = {};
                this.form.memberCouponId = null;
                try {
                    this.confirmDetail = await this.api(`/thestar/order/member/detail/${r.orderId}`);
                } catch {
                    this.confirmDetail = [];
                }
            } catch (e) {
                this.toast('err', '預訂失敗', this.errMsg(e));
                // 可能是被搶訂造成庫存不足:回選房頁並重查空房,避免對著舊數字再選一次
                this.book.step = 'list';
                this.searchRooms();
            }
        },
        pay(orderId) {
            this.toast('warn', '前往綠界結帳', `No.${orderId}`);
            // 付完款瀏覽器導回的前端頁：本機開就回本機、ngrok 開就回 ngrok(依目前所在網域三元判斷)
            const backHost = (location.hostname === 'localhost' || location.hostname === '127.0.0.1')
                ? 'http://localhost:8080'
                : location.origin;
            const clientBackUrl = encodeURIComponent(`${backHost}/result.html`);
            window.location.href = `/thestar/ecpay/checkout/${orderId}?clientBackUrl=${clientBackUrl}`;
        },

        /* ===== 優惠券 ===== */
        async loadCoupons() {
            this.couponsLoading = true;
            try {
                this.coupons = await
                    this.api('/api/member/coupons');
                return true;
            } catch (e) {
                this.coupons = [];
                this.toast('err', '優惠券載入失敗', this.errMsg(e));
                return false;
            } finally {
                this.couponsLoading = false;
            }
        },

        resolveCouponDiscount(memberCouponId, totalAmount) {
            if (memberCouponId == null || totalAmount <= 0) return 0;

            const coupon = this.coupons.find(c => c.memberCouponId === memberCouponId);
            if (!coupon) return 0;

            if (Number(coupon.discountType) === 1) {                      // 固定金額:金額須超過折抵才有折扣
                const discountAmount =
                    Number(coupon.discountAmount || 0);

                if (totalAmount <= discountAmount) {
                    return 0;
                }

                return discountAmount;
            }
            if (coupon.discountType === 2) {                              // 百分比(實付比例)
                const paid = Math.round(totalAmount * (coupon.discountPercent || 100) / 100);
                return totalAmount - paid;
            }
            return 0;
        },
        // 券的折扣文字(下拉選項 & 預覽卡顯示用)
        couponDiscountText(coupon) {
            if (!coupon) return '';
            if (coupon.discountType === 1) {
                return `折抵 $${(Number(coupon.discountAmount) || 0).toLocaleString()}`;
            }
            if (coupon.discountType === 2) {
                return `${(Number(coupon.discountPercent) || 100) / 10} 折`;
            }
            return '優惠內容未設定';
        },
        // 到期日只取 yyyy-MM-dd
        couponEndDate(value) {
            return value ? String(value).slice(0, 10) : '';
        },
        // 這張券現在能不能用:訂單金額要「超過」折抵金額(= 至少折抵+1)才算達門檻
        // 百分比券沒有 discountAmount(視為 0),一律可用
        couponCanUse(coupon) {
            if (!coupon) return false;
            return this.bookTotal >= Number(coupon.discountAmount || 0) + 1;
        },
        // 下拉選項文字:未達門檻的券會被 disabled 反灰,文字尾端加註原因
        couponOptionText(coupon) {
            const base = `${coupon.couponName} ｜${this.couponDiscountText(coupon)} ｜${this.couponEndDate(coupon.usageEndTime)} 到期`;
            return this.couponCanUse(coupon) ? base : `${base}（未達門檻）`;
        },
    },
};
