const { createApp } = Vue;

createApp({
  data() {
    return {
      loading: true,
      saving: false,
      editing: false,
      profile: {},
      form: {
        memberName: '',
        memberPhone: '',
        memberAddress: '',
        memberBirthday: null,
        memberGender: 2
      },
      pictureVersion: Date.now(),
      pictureVisible: true,
      selectedPictureFile: null,
      previewPictureUrl: '',
      errorMsg: '',
      successMsg: ''
    };
  },

  computed: {
    profilePictureUrl() {
      if (!this.pictureVisible) {
        return '';
      }

      return `/api/member/profile/picture?v=${this.pictureVersion}`;
    }
  },

  mounted() {
    this.loadProfile();
  },

  beforeUnmount() {
    this.revokePreviewUrl();
  },

  methods: {
    genderText(value) {
      if (value === 0) return '女';
      if (value === 1) return '男';
      if (value === 2) return '不透露';
      return '未填寫';
    },

    statusText(value) {
      if (value === 0) return '未啟用';
      if (value === 1) return '啟用';
      if (value === 2) return '停用';
      return '未知';
    },

    safeText(value) {
      if (value === null || value === undefined || value === '') {
        return '未填寫';
      }

      return String(value);
    },

    clearMessage() {
      this.errorMsg = '';
      this.successMsg = '';
    },

    hideBrokenPhoto() {
      this.pictureVisible = false;
    },

    refreshPicture() {
      this.pictureVisible = true;
      this.pictureVersion = Date.now();
    },

    async loadProfile() {
      this.clearMessage();
      this.loading = true;

      try {
        const res = await fetch('/api/member/profile', {
          credentials: 'same-origin'
        });

        const data = await res.json().catch(() => ({}));

        if (res.status === 401) {
          location.href = '/login.html?redirect=/profile.html';
          return;
        }

        if (!res.ok) {
          this.errorMsg = data.error || '讀取會員資料失敗';
          return;
        }

        this.profile = data;
        this.refreshPicture();
      } catch (e) {
        this.errorMsg = '無法連線到伺服器';
      } finally {
        this.loading = false;
      }
    },

    enterEditMode() {
      this.clearMessage();
      this.revokePreviewUrl();
      this.selectedPictureFile = null;

      this.form = {
        memberName: this.profile.memberName || '',
        memberPhone: this.profile.memberPhone || '',
        memberAddress: this.profile.memberAddress || '',
        memberBirthday: this.profile.memberBirthday || null,
        memberGender: this.profile.memberGender ?? 2
      };

      this.editing = true;
    },

    exitEditMode() {
      this.clearMessage();
      this.revokePreviewUrl();
      this.selectedPictureFile = null;
      this.editing = false;
    },

    validateProfile() {
      if (!this.form.memberName) {
        return '姓名不可空白';
      }

      if (this.form.memberPhone && !/^09\d{8}$/.test(this.form.memberPhone)) {
        return '手機格式錯誤，請輸入 09 開頭共 10 碼';
      }

      return '';
    },

    validatePicture(file) {
      const allowTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
      const maxSize = 5 * 1024 * 1024;

      if (!allowTypes.includes(file.type)) {
        return '照片格式只支援 JPG、PNG、GIF、WEBP';
      }

      if (file.size > maxSize) {
        return '照片不可超過 5MB';
      }

      return '';
    },

    handlePictureChange(event) {
      this.clearMessage();

      const file = event.target.files[0];

      if (!file) {
        return;
      }

      const error = this.validatePicture(file);

      if (error) {
        this.errorMsg = error;
        event.target.value = '';
        return;
      }

      this.revokePreviewUrl();
      this.selectedPictureFile = file;
      this.previewPictureUrl = URL.createObjectURL(file);
    },

    clearSelectedPicture() {
      this.revokePreviewUrl();
      this.selectedPictureFile = null;

      const fileInput = document.querySelector('input[type="file"]');
      if (fileInput) {
        fileInput.value = '';
      }
    },

    revokePreviewUrl() {
      if (this.previewPictureUrl) {
        URL.revokeObjectURL(this.previewPictureUrl);
        this.previewPictureUrl = '';
      }
    },

    async uploadPictureIfNeeded() {
      if (!this.selectedPictureFile) {
        return;
      }

      const formData = new FormData();
      formData.append('picture', this.selectedPictureFile);

      const res = await fetch('/api/member/profile/picture', {
        method: 'POST',
        credentials: 'same-origin',
        body: formData
      });

      const data = await res.json().catch(() => ({}));

      if (res.status === 401) {
        location.href = '/login.html?redirect=/profile.html';
        return;
      }

      if (!res.ok) {
        throw new Error(data.error || '照片更新失敗');
      }

      this.clearSelectedPicture();
      this.refreshPicture();
    },

    async saveProfile() {
      this.clearMessage();

      const error = this.validateProfile();

      if (error) {
        this.errorMsg = error;
        return;
      }

      this.saving = true;

      const payload = {
        memberName: this.form.memberName,
        memberPhone: this.form.memberPhone,
        memberAddress: this.form.memberAddress,
        memberBirthday: this.form.memberBirthday || null,
        memberGender: Number(this.form.memberGender)
      };

      try {
        const res = await fetch('/api/member/profile', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'same-origin',
          body: JSON.stringify(payload)
        });

        const data = await res.json().catch(() => ({}));

        if (res.status === 401) {
          location.href = '/login.html?redirect=/profile.html';
          return;
        }

        if (!res.ok) {
          this.errorMsg = data.error || '修改失敗';
          return;
        }

        this.profile = data;
        await this.uploadPictureIfNeeded();

        this.editing = false;
        this.successMsg = '會員資料修改成功';
      } catch (e) {
        this.errorMsg = e.message || '無法連線到伺服器';
      } finally {
        this.saving = false;
      }
    }
  }
}).mount('#app');
