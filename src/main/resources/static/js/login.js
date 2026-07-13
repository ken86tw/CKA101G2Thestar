const { createApp } = Vue;

createApp({
  data() {
    return {
      submitting: false,
      resending: false,
      redirect: '/index.html',
      errorMsg: '',
      successMsg: '',
      showResendVerify: false,
      form: {
        memberEmail: '',
        memberPassword: '',
        rememberMe: false
      }
    };
  },

  mounted() {
	const params = new URLSearchParams(location.search);
	this.redirect = this.normalizeRedirect(params.get('redirect'));

    const rememberedEmail = localStorage.getItem('theStarRememberEmail') || '';
    const rememberedPassword = localStorage.getItem('theStarRememberPassword') || '';
    this.form.memberEmail = rememberedEmail;
    this.form.memberPassword = rememberedPassword;
    this.form.rememberMe = rememberedEmail !== '' || rememberedPassword !== '';

    if (params.get('verified') === '1') {
      this.successMsg = '信箱驗證成功，請登入會員';
    }
  },

  methods: {
    normalizeRedirect(value) {
      if (!value || !value.startsWith('/') || value.startsWith('//')) {
        return '/index.html';
      }

      try {
        const target = new URL(value, location.origin);

        if (target.origin !== location.origin) {
          return '/index.html';
        }

        return target.pathname + target.search + target.hash;
      } catch (e) {
        return '/index.html';
      }
    },

    clearMessage() {
      this.errorMsg = '';
      this.successMsg = '';
      this.showResendVerify = false;
    },

    async submitLogin() {
      this.clearMessage();

      if (!this.form.memberEmail || !this.form.memberPassword) {
        this.errorMsg = '請輸入信箱與密碼';
        return;
      }

      this.submitting = true;

      try {
        const res = await fetch('/api/member/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'same-origin',
          body: JSON.stringify({
            memberEmail: this.form.memberEmail,
            memberPassword: this.form.memberPassword
          })
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
          this.errorMsg = data.error || '登入失敗';
          this.showResendVerify = this.errorMsg.includes('帳號尚未完成信箱驗證');
          return;
        }

        if (this.form.rememberMe) {
          localStorage.setItem('theStarRememberEmail', this.form.memberEmail);
          localStorage.setItem('theStarRememberPassword', this.form.memberPassword);
        } else {
          localStorage.removeItem('theStarRememberEmail');
          localStorage.removeItem('theStarRememberPassword');
        }

        location.href = this.redirect;
      } catch (err) {
        this.errorMsg = '無法連線到伺服器';
      } finally {
        this.submitting = false;
      }
    },

    async resendVerification() {
      this.errorMsg = '';
      this.successMsg = '';

      if (!this.form.memberEmail) {
        this.errorMsg = '請先輸入會員信箱';
        return;
      }

      this.resending = true;

      try {
        const res = await fetch('/api/member/resend-verification', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'same-origin',
          body: JSON.stringify({
            memberEmail: this.form.memberEmail
          })
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
          this.errorMsg = data.error || '驗證信重送失敗';
          return;
        }

        this.successMsg = data.message || '驗證信已重新寄出，請到信箱收信';
        this.showResendVerify = false;
      } catch (err) {
        this.errorMsg = '無法連線到伺服器';
      } finally {
        this.resending = false;
      }
    }
  }
}).mount('#app');
