/* ============================================================
  「房務管理」(員工)
  辦理入住(查明細/配房/旅客照片)、辦理退房、
  住宿紀錄查詢與詳情彈窗
   ============================================================ */
window.RB = window.RB || {};

RB.stay = {
    computed: {
        stayRecordsFiltered() {
            const list = this.stay.records;
            return this.stay.find.hideCheckedOut ? list.filter(r => !r.checkOutTime) : list;
        },
    },
    methods: {
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
                this.stay.lines = await this.api(`/thestar/admin/stayrecord/checkin-order/${this.stay.orderId}`);
                this.stay.checkin.roomId = null;
                this.stay.checkin.stayCustomer = '';
                if (this.stay.checkinPhotoUrl) URL.revokeObjectURL(this.stay.checkinPhotoUrl);
                this.stay.checkinPhoto = null;
                this.stay.checkinPhotoUrl = null;
                if (this.stay.lines.every(l => l.remaining <= 0)) {
                    this.stay.showModal = false;
                    this.stay.rooms = [];
                } else {
                    this.stay.rooms = await this.api(`/thestar/admin/stayrecord/checkin-rooms/${this.stay.activeListId}`);
                }

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

                this.stay.checkoutRoomId = null;
                if (this.stay.staying.length) this.loadStaying();
            } catch (e) {
                this.toast('err', '退房失敗', this.errMsg(e));
            }
        },

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
    },
};
