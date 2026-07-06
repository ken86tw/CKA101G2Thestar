const { createApp } = Vue;

createApp({
  data() {
    return {
      loading: true,
      success: false,
      message: ''
    };
  },

  mounted() {
    this.verifyToken();
  },

  methods: {
    async verifyToken() {
      const params = new URLSearchParams(location.search);
      const token = params.get('token') || '';

      if (!token) {
        this.loading = false;
        this.success = false;
        this.message = '驗證連結缺少 token';
        return;
      }

      try {
        const res = await fetch(`/api/member/verify?token=${encodeURIComponent(token)}`, {
          credentials: 'same-origin'
        });

        const data = await res.json().catch(() => ({}));

        this.loading = false;
        this.success = res.ok;
        this.message = data.message || data.error || (res.ok ? '信箱驗證成功' : '驗證失敗');
      } catch (e) {
        this.loading = false;
        this.success = false;
        this.message = '無法連線到伺服器';
      }
    }
  }
}).mount('#app');
