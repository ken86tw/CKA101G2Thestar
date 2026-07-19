/* ============================================================
   roombooking/main.js — Vue app 入口(一定要排在最後載入)
   內容:全頁狀態 data()、watch、mounted 初始化,
        並把各功能檔(RB.xxx)的 computed/methods 合併進同一個 app。
   data() 全放這裡當「狀態總覽」:哪個分頁用哪塊資料一眼看完。
   ============================================================ */
const {createApp} = Vue;

createApp({
    data() {
        const d = localDate;
        // 伺服器塞在模板裡的員工旗子(roombooking.html 檔尾的 window.IS_EMPLOYEE):
        // data() 比第一次渲染早,所以員工開頁第一幀就直接是後台分頁,
        // 不會先畫查房首屏、等 /thestar/admin/me 回來才跳過去(閃 0.幾秒的元凶)
        const isEmp = window.IS_EMPLOYEE === true;
        return {
            STATUS,
            isEmp,
            nav: isEmp ? 'admin' : 'book',
            /* --- 訂房流程(booking.js) --- */
            roomTypes: [],   // 房型清單(id/name/price),頁面載入時從查房API撈,新增房型自動跟上
            form: {checkInDate: d(1), checkOutDate: d(2), memberCouponId: null, rooms: [{roomTypeId: null, qty: 1}]},
            coupons: [],
            couponsLoading: false,
            book: {step: 'search', results: [], sel: {}, nights: 0},
            browsing: !isEmp,   // 訪客/會員直接進查房頁不擋登入,到「確認預訂」才要求登入;員工一進來就是 false,不顯示訂房分頁
            // 「還原訂單中」旗子:data() 是同步執行、比第一次畫面渲染還早,
            // 這裡先檢查瀏覽器暫存有沒有登入前存的訂房內容——有就先不畫查房首屏
            // (改成顯示還原提示),等 mounted 的還原流程跑完才放下,畫面就不會先閃一下查房頁。
            // !! 是把「字串或 null」轉成「true/false」
            restoring: !!sessionStorage.getItem('pendingBooking'),
            confirmOrder: null,
            confirmDetail: [],
            /* --- 登入身分 --- */
            member: {id: 1, on: null, name: ''},
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
            inv: {date: '', list: []},   // 庫存查詢:date=查詢起日, list=後端回來的 155 筆原始資料
            /* --- 共用(common.js) --- */
            toasts: [],
            logLines: [],
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
        // searchbarOn 從 false 變 true(例如從確認頁按「更改日期」回搜尋頁)→ 重掛日曆。
        // $nextTick:Vue 改資料後「下一拍」才真正把 input 放回頁面,直接掛會找不到元素,要等這一拍
        searchbarOn(on) {
            if (on) this.$nextTick(this.initDatePicker);
        }
    },
    // 重整後問會員/員工登入狀態:兩種身分都存在同一個 HTTP session,重整不掉登入
    async mounted() {
        // 全站唯一的一個時鐘 每秒更新nowTick 所有倒數畫面都靠它驅動
        // 頁面關掉瀏覽器就會收掉它 所以不需要手動clearInterval
        setInterval(() => this.nowTick = Date.now(), 1000);
        // 第一次進頁面就把日期日曆掛上搜尋列(上面的 watch 只管「離開又回來」的重掛,首次要自己掛)
        this.initDatePicker();
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
        // 共用導覽列會員選單的「我的訂房預訂」會連到 /roombooking.html?tab=orders:
        // 登入會員帶這個參數進來就直接切到「我的預訂」分頁(員工在下面會被改成 admin,不受影響)
        if (this.member.on && new URLSearchParams(location.search).get('tab') === 'orders') {
            this.nav = 'orders';
            this.loadOrders();
        }
        // 員工從後台登入頁(Spring Security 的 /thestar/admin/login)登入後進來:共用同一個 session,
        // /thestar/admin/me 登入回員工資料、未登入回 401。認得員工就直接進後台
        try {
            const emp = await this.api('/thestar/admin/me');
            this.employee.on = emp.employeeId;
            this.employee.name = emp.employeeName;
            this.browsing = false;   // 關掉訪客瀏覽旗:員工不該看到「預訂客房」分頁與查房頁
            this.connectWs();   // 員工身分確認後連線 訂閱 rooms/orders/refunds 頻道
            this.nav = 'admin';
            this.loadAdmin();
        } catch { /* 不是登入員工就略過,維持一般查房頁 */
        }
        // 登入前有存下的訂房內容:重查空房(拿最新剩餘數)後還原選擇,直接跳回確認頁。
        // 還原期間 restoring 旗子擋住查房首屏;finally 保證不管成功失敗旗子都會放下,
        // 不然中途出錯的話畫面會永遠卡在「還原中」
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
                // await:等 goConfirm 裡的優惠券載完、step 真的切到 confirm 才往下走,
                // 旗子才不會太早放下又閃一次查房頁
                if (this.bookItems.length) await this.goConfirm();
                else this.toast('warn', '空房狀況已變動', '請重新選擇客房');
            } finally {
                this.restoring = false;
            }
        } else {
            // 沒登入(登入到一半跑回來)或沒有暫存單:放下旗子,照常顯示查房頁
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
