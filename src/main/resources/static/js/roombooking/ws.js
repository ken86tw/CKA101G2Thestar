/* ============================================================
   WebSocket即時通知
   員工訂閱 rooms/orders/refunds 頻道、
   會員訂閱(退款完成/退房完成/逾時取消)
   ============================================================ */
window.RB = window.RB || {};

RB.ws = {
    methods: {
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

                        if (this.nav === 'refund') this.loadRefunds();

                        if (this.nav === 'admin') this.loadAdmin();
                    });
                    client.subscribe('/topic/orders', (msg) => {
                        const data = JSON.parse(msg.body);
                        const text ={created:'有新訂單成立',paid:'有訂單付款已完成',completed:'有訂單退房已完成'}[data?.event] ?? '訂單變動';
                        this.log('WS orders', '訂單變動');
                        this.toast('ok','訂單通知',text);
                        if (this.nav === 'admin') this.loadAdmin();
                    });
                }
                if (this.member.on) {

                    client.subscribe(`/topic/member/${this.member.on}`, (msg) => {
                        const data = JSON.parse(msg.body);

                        if (data.event === 'refunded') this.toast('ok', '退款完成', '您的訂單狀態已更新');
                        if (data.event === 'completed') this.toast('ok', '退房完成', '感謝您的入住');
                        if (data.event === 'canceled') {
                            // 收到取消通知先不動作 推遲一秒再處理
                            // setTimeout(要做的事, 毫秒數) 一秒後才執行大括號裡的內容
                            setTimeout(() => {
                                this.toast('ok', '訂單取消', '由於您逾時未付款訂單已取消');
                                // 若客人還停在確認頁 把頁面收掉 避免對著已取消的單按付款
                                this.confirmOrder = null;
                                this.loadOrders();
                            }, 1000);
                            // return 讓下面那行 loadOrders 不要立刻跑 統一等一秒後那次
                            return;
                        }
                        this.loadOrders();
                    });
                }
            };
            client.activate();
            this._stomp = client;
        },
    },
};
