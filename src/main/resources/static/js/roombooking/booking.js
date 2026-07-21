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

        searchbarOn() {

            return (this.member.on || this.browsing) && this.nav === 'book'
                && !this.confirmOrder && this.book.step === 'search' && !this.restoring;
        },
    },
    methods: {

        initDatePicker() {
            const el = this.$refs.dateRange;
            if (!el) return;

            if (this._fp) this._fp.destroy();

            this._fp = flatpickr(el, {
                mode: 'range',
                showMonths: 2,
                minDate: 'today',
                locale: 'zh_tw',
                dateFormat: 'Y-m-d',
                defaultDate: [this.form.checkInDate, this.form.checkOutDate],

                onChange: (dates, _str, fp) => {
                    if (dates.length === 2) {

                        this.form.checkInDate = fp.formatDate(dates[0], 'Y-m-d');
                        this.form.checkOutDate = fp.formatDate(dates[1], 'Y-m-d');
                    }
                }
            });
        },
        dateZh(s) {
            if (!s) return '';
            const [y, m, d] = s.split('-');

            const w = '日一二三四五六'[new Date(y, m - 1, d).getDay()];
            // Number() 順手把 '07' 的開頭 0 拿掉:7月 比 07月 順眼
            return `${y}年${Number(m)}月${Number(d)}日（${w}）`;
        },
        async loadRoomTypes() {
            try {
                const r = await this.api(`/find/room?checkInDate=${localDate()}&checkOutDate=${localDate(1)}`);
                this.roomTypes = r.map(x => ({
                    id: x.roomTypeId,
                    name: x.roomTypeName,
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
                    roomTypeName: x.roomTypeName,
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
                            '所選優惠券已失效或未達使用條件，請重新選擇'
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
