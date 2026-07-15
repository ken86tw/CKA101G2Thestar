const {createApp} = Vue;
const STATUS = [
    {v: 0, label: '待付款'}, {v: 1, label: '已付款'}, {v: 2, label: '已完成'}, {v: 3, label: '已取消'},
];
// 取本地日期字串yyyy-MM-dd(可加減天數)。不能用toISOString:那是UTC,台灣凌晨0點到早上8點會差一天
const localDate = (offsetDays = 0) => {
    const d = new Date(Date.now() + offsetDays * 864e5);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};
createApp({
    data() {
        const d = localDate;
        return {
            STATUS,
            nav: 'book',
            gateTab: 'member',
            roomTypes: [],   // 房型清單(id/name/price),頁面載入時從查房API撈,新增房型自動跟上
            member: {id: 1, on: null, name: ''},
            employee: {id: 1, on: null, name: ''},
            form: {checkInDate: d(1), checkOutDate: d(2), memberCouponId: null, rooms: [{roomTypeId: null, qty: 1}]},
            coupons: [],
            couponsLoading: false,
            book: {step: 'search', results: [], sel: {}, nights: 0},
            browsing: true,   // 預設直接進查房頁不擋登入,到「確認預訂」才要求登入;設 false 會顯示登入閘門(員工入口用)
            confirmOrder: null,
            confirmDetail: [],
            orders: {status: 0, page: 0, size: 10, list: [], totalPages: 0, counts: {}, open: {}, detail: {}},
            admin: {status: 0, page: 0, size: 10, list: [], totalPages: 0, counts: {}, open: {}, detail: {}},
            stay: {
                orderId: null,
                showModal: false,
                showCheckout: false,
                pending: [],
                lines: [],
                activeListId: null,
                activeTypeName: '',
                rooms: [],
                checkin: {orderListId: null, roomId: null, stayCustomer: ''},
                checkoutRoomId: null,
                staying: [],
                // hideCheckedOut是純前端過濾旗子 不會被findStay送到後端(它只挑前四個欄位組查詢字串)
                find: {roomId: null, stayCustomer: '', checkInTime: '', checkOutTime: '', hideCheckedOut: false},
                records: [],
                checkinPhoto: null,
                showDetail: false,
                detail: null,
                detailPhotoOk: true,
                checkinPhotoUrl: null,
                detailRecord: null,
                detailLines: []
            },
            refund: {list: []},
            toasts: [],
            logLines: [],
            _tid: 0,
        };
    },
    computed: {
        logText() {
            return this.logLines.join('\n') || '（尚無紀錄）';
        },

        stayRecordsFiltered() {
            const list = this.stay.records;
            return this.stay.find.hideCheckedOut ? list.filter(r => !r.checkOutTime) : list;
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
        couponDiscount(){
            return this.resolveCouponDiscount(this.form.memberCouponId,this.bookTotal);
        },
        // 目前選中的券物件(給預覽卡顯示用)
        selectedCoupon() {
            if (this.form.memberCouponId == null) return null;
            return this.coupons.find(c => c.memberCouponId === this.form.memberCouponId) || null;
        }
    },
    // 重整後問會員登入狀態(首頁的真登入);員工登入不跨頁記憶,重整需重登
    async mounted() {
        this.loadRoomTypes();   // 首屏房型展示卡用,不需登入
        try {
            const me = await this.api('/api/member/status');
            if (me.loggedIn) {
                this.member.id = me.memberId;
                this.member.on = me.memberId;
                this.member.name = me.memberName;
                this.connectWs();   // 會員身分確認後連線 訂閱自己專屬的通知頻道
            }
        } catch { /* 沒登入就停在登入頁 */
        }
        // 登入前有存下的訂房內容:重查空房(拿最新剩餘數)後還原選擇,直接跳回確認頁
        if (this.member.on && sessionStorage.getItem('pendingBooking')) {
            const p = JSON.parse(sessionStorage.getItem('pendingBooking'));
            sessionStorage.removeItem('pendingBooking');
            this.form.checkInDate = p.checkInDate;
            this.form.checkOutDate = p.checkOutDate;
            await this.searchRooms();
            for (const r of this.book.results) {
                const want = (p.sel || {})[r.roomTypeId] || 0;
                this.book.sel[r.roomTypeId] = Math.min(want, r.remaining); // 空房變少就自動下修
            }
            if (this.bookItems.length) this.goConfirm();
            else this.toast('warn', '空房狀況已變動', '請重新選擇客房');
        }
    },
    methods: {
        fmt(s) {
            return s ? String(s).replace('T', ' ').slice(0, 16) : '';
        },
        errMsg(e) {
            const b = e && e.body;
            if (b && typeof b === 'object') return b.error || b.message || JSON.stringify(b);
            return (typeof b === 'string' && b) ? b : '請稍後再試';
        },
        toast(type, title, msg = '') {
            const id = ++this._tid;
            this.toasts.push({id, type, title, msg});
            setTimeout(() => {
                this.toasts = this.toasts.filter(t => t.id !== id);
            }, 3400);
        },
        log(label, data) {
            const ts = new Date().toLocaleTimeString();
            this.logLines.unshift(`[${ts}] ${label}: ${typeof data === 'string' ? data : JSON.stringify(data)}`);
            if (this.logLines.length > 60) this.logLines.pop();
        },
        async api(url, opts = {}) {
            const res = await fetch(url, {credentials: 'same-origin', ...opts});
            const text = await res.text();
            let body;
            try {
                body = JSON.parse(text);
            } catch {
                body = text;
            }
            if (!res.ok) {
                this.log(`✘ ${res.status} ${url}`, body);
                throw {status: res.status, body};
            }
            return body;
        },
        async loginEmployee() {
            try {
                await this.api(`/dev/employeelogin/${this.employee.id}`);
                this.employee.on = this.employee.id;
                this.connectWs();
                this.toast('ok', '員工已登入', `#${this.employee.id}`);
                this.nav = 'admin';
                this.loadAdmin();
            } catch {
                this.toast('err', '登入失敗');
            }
        },
        async logout() {
            // 會員走首頁的真登出;員工端目前無登出API,清前端狀態即可
            if (this.member.on) {
                try {
                    await this.api('/api/member/logout', {method: 'POST'});
                } catch { /* 後端沒清成也照樣登出前端 */
                }
            }
            if (this._stomp) {
                this._stomp.deactivate();
                this._stomp = null;
            }
            this.member.on = null;
            this.member.name = '';
            this.employee.on = null;
            this.nav = 'book';
            this.orders.list = [];
            this.admin.list = [];
            this.toast('warn', '已登出');
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

        goConfirm() {
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
            this.loadCoupons();
            this.book.step = 'confirm';
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
                this.confirmOrder = r;
                this.book.step = 'search';
                this.book.results = [];
                this.book.sel = {};
                this.form.memberCouponId = null;
                try {
                    this.confirmDetail = await this.api(`/thestar/order/member/order/detail/${r.orderId}`);
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
        async loadOrders() {
            if (!this.member.on) return;
            try {
                const r = await this.api(`/thestar/order/member/order?orderStatus=${this.orders.status}&page=${this.orders.page}&size=${this.orders.size}`);
                this.orders.list = r.content || [];
                this.orders.totalPages = r.totalPages || 0;
                this.orders.open = {};
                this.refreshCounts(this.orders, '/thestar/order/member/order');
            } catch {
                this.orders.list = [];
            }
        },
        async loadAdmin() {
            if (!this.employee.on) {
                this.toast('warn', '請先員工登入', '登出後改用員工身分');
                return;
            }
            try {
                const r = await this.api(`/thestar/admin/order?orderStatus=${this.admin.status}&page=${this.admin.page}&size=${this.admin.size}`);
                this.admin.list = r.content || [];
                this.admin.totalPages = r.totalPages || 0;
                this.admin.open = {};
                this.refreshCounts(this.admin, '/thestar/admin/order');
            } catch {
                this.admin.list = [];
            }
        },
        async refreshCounts(box, base) {
            for (const s of STATUS) {
                try {
                    const r = await this.api(`${base}?orderStatus=${s.v}&page=0&size=1`);
                    box.counts[s.v] = r.totalElements || 0;
                } catch {
                    box.counts[s.v] = 0;
                }
            }
        },
        async toggleDetail(box, orderId, kind) {
            if (box.open[orderId]) {
                box.open[orderId] = false;
                return;
            }
            try {
                const url = kind === 'admin' ? `/thestar/admin/order/detail/${orderId}` : `/thestar/order/member/order/detail/${orderId}`;
                box.detail[orderId] = await this.api(url);
                box.open[orderId] = true;
            } catch {
                this.toast('err', '明細查詢失敗');
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
        async devConfirm(orderId) {
            if (!confirm(`模擬付款（不經綠界，直接標記 No.${orderId} 已付款）？`)) return;
            try {
                await this.api(`/dev/confirm/${orderId}`);
                this.toast('ok', '模擬付款成功', `No.${orderId} → 已付款`);
                this.loadOrders();
            } catch (e) {
                this.toast('err', '模擬付款失敗', this.errMsg(e));
            }
        },
        async cancel(orderId) {
            const reason = prompt('取消原因？', '測試取消');
            if (reason === null) return;
            try {
                await this.api(`/thestar/order/cancel/${orderId}`, {
                    method: 'POST',
                    headers: {'Content-Type': 'text/plain'},
                    body: reason
                });
                this.toast('ok', '已取消', `No.${orderId}`);
                this.loadOrders();
            } catch (e) {
                this.toast('err', '取消失敗', this.errMsg(e));
            }
        },
        // 載入所有已付款且還沒配過房的訂單
        // 開入住彈窗:先把上次殘留的查詢狀態歸零再載入未入住清單
        openCheckinModal() {
            this.stay.orderId = null;
            this.stay.lines = [];
            this.stay.rooms = [];
            this.stay.activeListId = null;
            this.stay.activeTypeName = '';
            this.stay.checkin.orderListId = null;
            this.stay.checkin.roomId = null;
            this.stay.checkin.stayCustomer = '';
            this.stay.showModal = true;
            if (this.stay.checkinPhotoUrl)
                URL.revokeObjectURL(this.stay.checkinPhotoUrl);
            this.stay.checkinPhoto = null;
            this.stay.checkinPhotoUrl = null;
            this.loadPending();
        },
        // 開退房彈窗:清掉上次殘留的房號再載入在住清單
        openCheckoutModal() {
            this.stay.checkoutRoomId = null;
            this.stay.showCheckout = true;
            this.loadStaying();
        },
        async loadPending() {
            try {
                this.stay.pending = await this.api('/thestar/admin/order/find/order');
            } catch (e) {
                this.stay.pending = [];
                this.toast('err', '查詢失敗', this.errMsg(e));
            }
        },
        async loadCheckInLines() {
            if (!this.stay.orderId) {
                this.toast('warn', '請先輸入訂單 ID');
                return;
            }
            try {
                this.stay.lines = await this.api(`/thestar/admin/stayrecord/checkin-order/${this.stay.orderId}`);
                this.stay.rooms = [];
                this.stay.activeListId = null;
                this.stay.activeTypeName = '';
                this.stay.checkin.orderListId = null;
                this.stay.checkin.roomId = null;
                this.stay.showModal = true;
                this.toast('ok', '查詢完成', `共 ${this.stay.lines.length} 個房型`);
            } catch (e) {
                this.stay.lines = [];
                this.toast('err', '查詢失敗', this.errMsg(e));
            }
        },
        async openRooms(line) {
            if (line.remaining <= 0) {
                this.toast('warn', '此房型已配滿');
                return;
            }
            this.stay.activeListId = line.orderListId;
            this.stay.activeTypeName = line.roomTypeName;
            this.stay.checkin.orderListId = line.orderListId;
            this.stay.checkin.roomId = null;
            try {
                this.stay.rooms = await this.api(`/thestar/admin/stayrecord/checkin-rooms/${line.orderListId}`);
            } catch (e) {
                this.stay.rooms = [];
                this.toast('err', '房間載入失敗', this.errMsg(e));
            }
        },
        pickRoom(r) {
            if (r.roomStatus === 1 || r.roomSwitchStatus === false) return;  // 入住中/停用不可選
            this.stay.checkin.roomId = r.roomId;
        },
        pickStayPhoto(e) {
            const file = e.target.files[0] || null;
            if (this.stay.checkinPhotoUrl)
                URL.revokeObjectURL(this.stay.checkinPhotoUrl);
            this.stay.checkinPhoto = file;
            this.stay.checkinPhotoUrl = file ?
                URL.createObjectURL(file) : null;
        },
        async checkIn() {
            if (!this.stay.checkin.orderListId) {
                this.toast('warn', '請先在明細點「配房」');
                return;
            }
            if (!this.stay.checkin.roomId) {
                this.toast('warn', '請點一間空房');
                return;
            }
            try {
                const fd = new FormData();
                fd.append('dto', new Blob([JSON.stringify(this.stay.checkin)], {type: 'application/json'}));
                if (this.stay.checkinPhoto)
                    fd.append('stayCustomerPhoto',
                        this.stay.checkinPhoto);
                await this.api('/thestar/admin/stayrecord/checkin',
                    {
                        method: 'POST',
                        body: fd
                    });
                this.toast('ok', '入住成功', `房號 ${this.stay.checkin.roomId} · ${this.stay.checkin.stayCustomer || ''}`);
                // 重新整理:明細的已入住/剩餘 + 房間狀態(剛配的變紅)
                this.stay.lines = await this.api(`/thestar/admin/stayrecord/checkin-order/${this.stay.orderId}`);
                this.stay.rooms = await this.api(`/thestar/admin/stayrecord/checkin-rooms/${this.stay.activeListId}`);
                this.stay.checkin.roomId = null;
                this.stay.checkin.stayCustomer = '';
                if (this.stay.checkinPhotoUrl) URL.revokeObjectURL(this.stay.checkinPhotoUrl);
                this.stay.checkinPhoto = null;
                this.stay.checkinPhotoUrl = null;
                // 全部房型都配滿就自動關閉彈窗
                if (this.stay.lines.every(l => l.remaining <= 0)) {
                    this.stay.showModal = false;
                    this.stay.rooms = [];
                }
                // 有載過未入住清單就重整 配過房的訂單會從清單消失
                if (this.stay.pending.length) this.loadPending();
            } catch (e) {
                this.toast('err', '入住失敗', this.errMsg(e));
            }
        },
        async checkOut() {
            if (!this.stay.checkoutRoomId) {
                this.toast('warn', '請輸入退房房號');
                return;
            }
            if (!confirm(`房號 ${this.stay.checkoutRoomId} 確認退房？`)) return;
            try {
                const r = await this.api(`/thestar/admin/stayrecord/checkout/${this.stay.checkoutRoomId}`, {method: 'POST'});
                this.toast('ok', '退房成功', r);
                // 退房後清空房號 有載過在住清單就重整讓退掉的那間消失
                this.stay.checkoutRoomId = null;
                if (this.stay.staying.length) this.loadStaying();
            } catch (e) {
                this.toast('err', '退房失敗', this.errMsg(e));
            }
        },
        // 載入所有未退房的在住房間
        async loadStaying() {
            try {
                this.stay.staying = await this.api('/thestar/admin/stayrecord/find/all');
            } catch (e) {
                this.stay.staying = [];
                this.toast('err', '查詢失敗', this.errMsg(e));
            }
        },
        async findStay() {
            try {
                const q = new URLSearchParams();
                const f = this.stay.find;
                if (f.roomId) q.set('roomId', f.roomId);
                if (f.stayCustomer) q.set('stayCustomer', f.stayCustomer);
                if (f.checkInTime) q.set('checkInTime', f.checkInTime);
                if (f.checkOutTime) q.set('checkOutTime', f.checkOutTime);
                this.stay.records = await this.api(`/thestar/admin/stayrecord/find?${q}`);
                this.toast('ok', '查詢完成', `共 ${this.stay.records.length} 筆`);
            } catch (e) {
                this.toast('err', '查詢失敗', this.errMsg(e));
            }
        },
        async openStayDetail(r) {
            this.stay.detailRecord = r;
            this.stay.detail = null;
            this.stay.detailLines = [];
            // 照片旗子重設 上一筆沒照片會被@error設成false 不重設的話這筆有照片也會顯示「無照片」
            this.stay.detailPhotoOk = true;
            this.stay.showDetail = true;
            try {
                this.stay.detail = await this.api(`/thestar/admin/stayrecord/find/order/${r.stayId}`);
                this.stay.detailLines = await this.api(`/thestar/admin/order/detail/${this.stay.detail.orderId}`);

            } catch (e) {
                this.toast('err', '詳情查詢失敗', this.errMsg(e));
            }
        },

        async loadRefunds() {
            try {
                this.refund.list = await
                    this.api('/thestar/admin/refund/find');
            } catch (e) {
                this.refund.list = [];
                this.toast('err', '退款清單查詢失敗', this.errMsg(e));
            }
        },

        async doRefund(r) {
            if (!confirm(`退款單 #${r.refundId} · $${r.amount} 確認執行退款？`)) return;
            try {
                const msg = await
                    this.api(`/thestar/admin/refund/process/${r.refundId}`, {method: 'POST'});
                this.toast('ok', '退款完成', msg);
                this.loadRefunds();
            } catch (e) {
                this.toast('err', '退款失敗', this.errMsg(e));
            }
        },
        async loadCoupons() {
            this.couponsLoading = true;
            try {
                this.coupons = await
                    this.api('/api/member/coupons');
            } catch (e) {
                this.coupons = [];
                this.toast('err', '優惠券載入失敗', this.errMsg());

            } finally {
                this.couponsLoading = false;
            }
        },

        resolveCouponDiscount(memberCouponId, totalAmount) {
            if (memberCouponId == null || totalAmount <= 0) return 0;

            const coupon = this.coupons.find(c => c.memberCouponId === memberCouponId);
            if (!coupon) return 0;

            if (coupon.discountType === 1) {                              // 固定金額
                return Math.min(totalAmount, coupon.discountAmount || 0);
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



        connectWs() {
            if (this._stomp) return;
            const proto = location.protocol === 'https:' ? 'wss' : 'ws';
            const client = new StompJs.Client({
                brokerURL: `${proto}://${location.host}/ws`,
                reconnectDelay: 5000,
            });
            client.onConnect = () => {
                this.log('✔ WS', '已連線');
                if (this.employee.on) {
                    client.subscribe('/topic/rooms', (msg) => {
                        const data = JSON.parse(msg.body);
                        this.log('WS rooms', data);
                        const room = this.stay.rooms.find(r => r.roomId ===
                            data.roomId);
                        if (room) room.roomStatus = data.roomStatus;
                    });
                    client.subscribe('/topic/refunds', () => {
                        this.log('WS refunds', '清單變動');
                        //取消訂單或退款完成都會敲這口鐘 人在哪個分頁就重查哪邊
                        if (this.nav === 'refund') this.loadRefunds();
                        //後台訂單的列表跟上面統計數字也會受影響 一起重查
                        if (this.nav === 'admin') this.loadAdmin();
                    });
                    client.subscribe('/topic/orders', () => {
                        this.log('WS orders', '訂單變動');
                        if (this.nav === 'admin') this.loadAdmin();
                    });
                }
                if (this.member.on) {
                    // 名字裡的ID是自己的 所以只會收到自己的通知
                    client.subscribe(`/topic/member/${this.member.on}`, (msg) => {
                        const data = JSON.parse(msg.body);
                        //同一個頻道兩種事件 看event決定跳哪句
                        if (data.event === 'refunded') this.toast('ok', '退款完成', '您的訂單狀態已更新');
                        if (data.event === 'completed') this.toast('ok', '退房完成', '感謝您的入住');
                        this.loadOrders();
                    });
                }
            };
            client.activate();
            this._stomp = client;
        },
    },
}).mount('#app');

