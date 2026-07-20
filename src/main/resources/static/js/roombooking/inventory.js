/* ============================================================
   roombooking/inventory.js — 「庫存查詢」(員工)
   31 天庫存查詢
   對應畫面:templates/roombooking/inventory.html
   ============================================================ */
window.RB = window.RB || {};

RB.inventory = {
    computed: {

        invTypes() {
            const names = [];
            for (const r of this.inv.list) {
                // includes = 問陣列「你裡面有這個值嗎」,沒有才 push,達成去重
                if (!names.includes(r.roomTypeName)) names.push(r.roomTypeName);
            }
            return names;
        },

        invRows() {

            const byDate = {};
            for (const r of this.inv.list) {
                if (!byDate[r.date]) byDate[r.date] = {};   // 等同 Java 的 computeIfAbsent
                byDate[r.date][r.roomTypeName] = r;
            }

            return Object.keys(byDate).sort().map(date => ({
                date,
                cells: this.invTypes.map(name => {
                    const r = byDate[date][name];

                    return r ? {remain: r.remainAmount, total: r.totalAmount}
                        : {remain: '-', total: '-'};
                })
            }));
        },
    },
    methods: {

        async loadInventory() {
            try {

                const url = this.inv.date
                    ? `/find/admin/room?date=${this.inv.date}`
                    : '/find/admin/room';
                this.inv.list = await this.api(url);
            } catch (e) {
                this.inv.list = [];
                this.toast('err', '庫存查詢失敗', this.errMsg(e));
            }
        },

        // ===== 庫存表格的顯示小工具=====

        // 依剩餘量決定格子顏色,回傳的字串會變成 td 的 class(對應 CSS 的三種水色)
        // 滿房 → 'full'(紅) / 有預訂(水位下降)→ 'low'(金) / 完全沒訂(滿水位)→ 'ok'(綠)
        invLevel(c) {
            if (typeof c.remain !== 'number') return '';
            if (c.remain === 0) return 'full';
            return c.remain < c.total ? 'low' : 'ok';
        },
        // 水位 = 剩餘比例,剩 7/10 → '70%'
        invPct(c) {
            if (typeof c.remain !== 'number' || !c.total) return '0%';
            return Math.round(c.remain / c.total * 100) + '%';
        },

        invMD(d) {
            const [, m, day] = d.split('-');
            return `${+m}/${+day}`;
        },
        invWD(d) {
            return '週' + '日一二三四五六'[new Date(d).getDay()];
        },
        // 週末的日期字改金色
        invIsWeekend(d) {
            const w = new Date(d).getDay();
            return w === 0 || w === 6;
        },

        invIsToday(d) {
            const t = new Date();
            const local = `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`;
            return d === local;
        },
    },
};
