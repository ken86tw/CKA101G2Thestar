/* ============================================================
   roombooking/main.js — Vue app 入口
   全頁狀態 data()、watch、mounted 初始化,
        並把各功能檔(RB.xxx)的 computed/methods 合併進同一個 app。
   data() 狀態總覽:哪個分頁用哪塊資料。
   ============================================================ */
const {createApp} = Vue;

createApp({
    data() {
        const d = localDate;
        const isEmp = window.IS_EMPLOYEE === true;
        return {
            STATUS,
            isEmp,
            nav: isEmp ? 'admin' : 'book',
            /* --- 訂房流程(booking.js) --- */
            roomTypes: [],
            form: {checkInDate: d(1), checkOutDate: d(2), memberCouponId: null, rooms: []},
            coupons: [],
            couponsLoading: false,
            book: {step: 'search', results: [], sel: {}, nights: 0},
            browsing: !isEmp,
            restoring: !!sessionStorage.getItem('pendingBooking'),
            confirmOrder: null,
            confirmDetail: [],
            /* --- 登入身分 --- */
            member: {on: null, name: ''},
            employee: {on: null, name: ''},
            /* --- 訂單列表(orders.js / admin-orders.js) --- */
            nowTick: Date.now(),   // 全站唯一的「現在時刻」 mounted裡每秒更新一次 所有倒數都拿訂單時間跟它相減算出來
            orders: {status: 0, page: 0, size: 10, list: [], totalPages: 0, counts: {}, open: {}, detail: {}},
            admin: {status: 0, page: 0, size: 10, list: [], totalPages: 0, counts: {}, open: {}, detail: {}},
            /* --- 房務管理(stay.js) --- */
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
            /* --- 退款管理(refund.js) --- */
            refund: {list: []},
            /* --- 庫存查詢(inventory.js) --- */
            inv: {date: '', list: []},   // 庫存查詢:date=查詢起日, list=後端回來的原始資料
            /* --- 共用(common.js) --- */
            toasts: [],
            _tid: 0,
        };
    },
    computed: {
        ...RB.common.computed,
        ...RB.booking.computed,
        ...RB.stay.computed,
        ...RB.inventory.computed,
    },
    // watch = 盯著某個資料,它一變就執行對應的函式(computed 是「算給畫面看」,watch 是「變了就做事」)
    watch: {

        searchbarOn(on) {
            if (on) this.$nextTick(this.initDatePicker);
        }
    },
    async mounted() {

        setInterval(() => this.nowTick = Date.now(), 1000);

        this.initDatePicker();
        this.loadRoomTypes();
        try {
            const me = await this.api('/api/member/status');
            if (me.loggedIn) {
                this.member.on = me.memberId;
                this.member.name = me.memberName;
                this.connectWs();
            }
        } catch {
        }

        if (this.member.on && new URLSearchParams(location.search).get('tab') === 'orders') {
            this.nav = 'orders';
            this.loadOrders();
        }

        try {
            const emp = await this.api('/thestar/admin/me');
            this.employee.on = emp.employeeId;
            this.employee.name = emp.employeeName;
            this.browsing = false;
            this.connectWs();
            this.nav = 'admin';
            this.loadAdmin();
        } catch {
        }

        if (this.member.on && sessionStorage.getItem('pendingBooking')) {
            try {
                const p = JSON.parse(sessionStorage.getItem('pendingBooking'));
                sessionStorage.removeItem('pendingBooking');
                this.form.checkInDate = p.checkInDate;
                this.form.checkOutDate = p.checkOutDate;
                await this.searchRooms();
                for (const r of this.book.results) {
                    const want = (p.sel || {})[r.roomTypeId] || 0;
                    this.book.sel[r.roomTypeId] = Math.min(want, r.remaining); // 空房變少就自動下修
                }

                if (this.bookItems.length) await this.goConfirm();
                else this.toast('warn', '空房狀況已變動', '請重新選擇客房');
            } finally {
                this.restoring = false;
            }
        } else {
            this.restoring = false;
        }
    },
    methods: {
        ...RB.common.methods,
        ...RB.booking.methods,
        ...RB.orders.methods,
        ...RB.adminOrders.methods,
        ...RB.stay.methods,
        ...RB.refund.methods,
        ...RB.inventory.methods,
        ...RB.ws.methods,
    },
}).mount('#app');
