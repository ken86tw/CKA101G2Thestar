/* ============================================================
   roombooking/admin-orders.js — 「後台訂單」(員工全站訂單列表)
   全站訂單分頁查詢
   對應畫面:templates/roombooking/admin-orders.html
   ============================================================ */
window.RB = window.RB || {};

RB.adminOrders = {
    methods: {
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
    },
};
