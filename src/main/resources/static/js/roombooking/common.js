window.RB = window.RB || {};

const STATUS = [
    {v: 0, label: '待付款'}, {v: 1, label: '已付款'}, {v: 2, label: '已完成'}, {v: 3, label: '已取消'},
];
// 取本地日期字串yyyy-MM-dd(可加減天數)。不能用toISOString:那是UTC,台灣凌晨0點到早上8點會差一天
const localDate = (offsetDays = 0) => {
    const d = new Date(Date.now() + offsetDays * 864e5);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

RB.common = {
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
                throw {status: res.status, body};
            }
            return body;
        },
        async logout() {
            // 只登出會員;員工的登出在後台做,這頁只提供「回後台」
            if (this.member.on) {
                try {
                    await this.api('/api/member/logout', {method: 'POST'});
                } catch { /* 後端沒清成也照樣登出前端 */
                }
            }
            // 員工還在線就別斷 WebSocket,rooms/orders/refunds 頻道還要用
            if (this._stomp && !this.employee.on) {
                this._stomp.deactivate();
                this._stomp = null;
            }
            this.member.on = null;
            this.member.name = '';
            this.orders.list = [];
            this.coupons = [];
            this.form.memberCouponId = null;
            if (!this.employee.on) this.nav = 'book';
            this.toast('warn', '已登出');
        },
        // 算一張訂單還剩幾秒可付款 確認頁跟訂單列表共用
        // 原理:下單時間+時限-現在=剩餘 全部用減法算 所以不用每張單各開一個計時器
        payLeftOf(o) {
            if (!o || !o.createdTime) return null;
            // createdTime是後端給的字串 new Date()解析成時間再取毫秒數
            const deadline = new Date(o.createdTime).getTime() + 200 * 1000;
            return Math.max(0, Math.floor((deadline - this.nowTick) / 1000)); // 到期停在0 不顯示負數
        },
        // 秒數轉 mm:ss 例如 200 → 3:20
        mmss(sec) {
            const m = Math.floor(sec / 60);
            const s = String(sec % 60).padStart(2, '0'); // 不足兩位補0 避免顯示 3:5
            return `${m}:${s}`;
        },
        // 「我的預訂」跟「後台訂單」共用:重查四個狀態的統計數字(box 傳 this.orders 或 this.admin)
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
        // 兩張訂單表共用:展開/收合一列的明細(kind 決定打會員 API 還是員工 API)
        async toggleDetail(box, orderId, kind) {
            if (box.open[orderId]) {
                box.open[orderId] = false;
                return;
            }
            try {
                const url = kind === 'admin' ? `/thestar/admin/order/detail/${orderId}` : `/thestar/order/member/detail/${orderId}`;
                box.detail[orderId] = await this.api(url);
                box.open[orderId] = true;
            } catch {
                this.toast('err', '明細查詢失敗');
            }
        },
    },
};
