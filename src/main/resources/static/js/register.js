const { createApp } = Vue;

createApp({
  data() {
    return {
      submitting: false,
      registered: false,
      mailSent: false,
      devVerifyUrl: '',
      errorMsg: '',
      successMsg: '',
      selectedPictureFile: null,
      previewPictureUrl: '',
      form: {
        memberName: '',
        memberEmail: '',
        memberPassword: '',
        confirmPassword: '',
        memberPhone: '',
        memberAddress: '',
        memberBirthday: '',
        memberGender: 2
      }
    };
  },

  beforeUnmount() {
    this.revokePreviewUrl();
  },

  methods: {
    clearMessage() {
      this.errorMsg = '';
      this.successMsg = '';
    },

    validateForm() {
      // 對應你原本 addMember.jsp / MemberServlet insert() 的判斷
      if (!this.form.memberName) {
        return '會員姓名請勿空白';
      }

      if (!this.form.memberEmail) {
        return '會員信箱請勿空白';
      }

      if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/.test(this.form.memberEmail)) {
        return '會員信箱格式不正確';
      }

      if (!this.form.memberPassword) {
        return '會員密碼請勿空白';
      }

      if (this.form.confirmPassword && this.form.memberPassword !== this.form.confirmPassword) {
        return '兩次輸入的密碼不一致';
      }

      if (!this.form.memberPhone) {
        return '會員手機請勿空白';
      }

      if (!/^09\d{8}$/.test(this.form.memberPhone)) {
        return '手機格式錯誤，請輸入 09 開頭的 10 碼手機號碼';
      }

      if (!this.form.memberAddress) {
        return '會員地址請勿空白';
      }

      if (![0, 1, 2].includes(Number(this.form.memberGender))) {
        return '會員性別格式不正確';
      }

      return this.validatePicture(this.selectedPictureFile);
    },

    validatePicture(file) {
      if (!file) {
        return '';
      }

      const maxSize = 5 * 1024 * 1024;
      if (file.size > maxSize) {
        return '圖片大小不可超過 5MB';
      }

      const allowTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
      if (file.type && !allowTypes.includes(file.type)) {
        return '照片格式只支援 JPG、PNG、GIF、WEBP';
      }

      return '';
    },

    handlePictureChange(event) {
      this.clearMessage();

      const file = event.target.files && event.target.files[0]
        ? event.target.files[0]
        : null;

      const error = this.validatePicture(file);
      if (error) {
        this.errorMsg = error;
        event.target.value = '';
        return;
      }

      this.revokePreviewUrl();
      this.selectedPictureFile = file;
      this.previewPictureUrl = file ? URL.createObjectURL(file) : '';
    },

    clearPicture() {
      const input = document.getElementById('memberPicture');
      if (input) {
        input.value = '';
      }

      this.selectedPictureFile = null;
      this.revokePreviewUrl();
    },

    revokePreviewUrl() {
      if (this.previewPictureUrl) {
        URL.revokeObjectURL(this.previewPictureUrl);
        this.previewPictureUrl = '';
      }
    },

    buildFormData() {
      const formData = new FormData();
      formData.append('memberName', this.form.memberName);
      formData.append('memberEmail', this.form.memberEmail);
      formData.append('memberPassword', this.form.memberPassword);
      formData.append('confirmPassword', this.form.confirmPassword || '');
      formData.append('memberPhone', this.form.memberPhone);
      formData.append('memberAddress', this.form.memberAddress);
      formData.append('memberGender', String(Number(this.form.memberGender)));

      if (this.form.memberBirthday) {
        formData.append('memberBirthday', this.form.memberBirthday);
      }

      if (this.selectedPictureFile) {
        formData.append('memberPicture', this.selectedPictureFile);
      }

      return formData;
    },

    async submitRegister() {
      this.clearMessage();

      const error = this.validateForm();

      if (error) {
        this.errorMsg = error;
        return;
      }

      this.submitting = true;

      try {
        const res = await fetch('/api/member/register', {
          method: 'POST',
          credentials: 'same-origin',
          body: this.buildFormData()
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
          this.errorMsg = data.error || '註冊失敗';
          return;
        }

        this.registered = true;
        this.mailSent = Boolean(data.mailSent);
        this.devVerifyUrl = data.devVerifyUrl || '';
        this.successMsg = data.message || '註冊成功，請完成信箱驗證';
        this.clearPicture();
      } catch (e) {
        this.errorMsg = '無法連線到伺服器';
      } finally {
        this.submitting = false;
      }
    }
  }
}).mount('#app');
