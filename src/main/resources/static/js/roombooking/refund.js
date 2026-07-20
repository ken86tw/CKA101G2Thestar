/* ============================================================
   「退款管理」(員工)
   未退款清單查詢、執行退款
   ============================================================ */
window.RB = window.RB || {};

RB.refund = {
    methods: {
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
            } catch (e) {
                this.toast('err', '退款失敗', this.errMsg(e));
            }
        },
    },
};
