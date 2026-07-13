(function () {
  async function getMemberStatus() {
    try {
      const res = await fetch('/api/member/status', {
        credentials: 'same-origin'
      });

      if (!res.ok) {
        return { loggedIn: false };
      }

      return await res.json();
    } catch (e) {
      return { loggedIn: false };
    }
  }

  async function logoutMember() {
    const currentPage =
      location.pathname + location.search + location.hash;

    let logoutTarget = currentPage;

    if (
      location.pathname === '/shop/cart' ||
      location.pathname.startsWith('/shop/cart/')
    ) {
      logoutTarget = '/shop';
    } else if (location.pathname === '/profile.html') {
      logoutTarget = '/index.html';
    } else if (
      location.pathname === '/roombooking.html' &&
      new URLSearchParams(location.search).get('tab') === 'orders'
    ) {
      logoutTarget = '/roombooking.html';
    }

    try {
      await fetch('/api/member/logout', {
        method: 'POST',
        credentials: 'same-origin'
      });
    } catch (e) {
    }

    if (logoutTarget === currentPage) {
      location.reload();
    } else {
      location.href = logoutTarget;
    }
  }

  function bindComingSoonLinks() {
    document.querySelectorAll('.js-coming-soon').forEach(function (link) {
      link.addEventListener('click', function (event) {
        event.preventDefault();
        const message = link.dataset.message || '此功能尚未完成';
        alert(message);
      });
    });
  }

  function bindNotifyButton() {
    const notifyBtn = document.getElementById('notifyBtn');

    if (!notifyBtn) {
      return;
    }

    notifyBtn.addEventListener('click', function () {
      alert('通知功能尚未完成');
    });
  }

  async function initHeaderMemberState() {
    const loginLink = document.getElementById('loginLink');
    const memberMenu = document.getElementById('memberMenu');
    const memberAvatar = document.getElementById('memberAvatar');
    const memberDropdown = document.getElementById('memberDropdown');
    const memberGreeting = document.getElementById('memberGreeting');
    const logoutBtn = document.getElementById('logoutBtn');

    if (!loginLink || !memberMenu || !memberAvatar || !memberDropdown || !logoutBtn) {
      return;
    }

    const currentPage = location.pathname + location.search + location.hash;
    loginLink.href = '/login.html?redirect=' + encodeURIComponent(currentPage || '/index.html');

    const status = await getMemberStatus();

    if (status.loggedIn) {
      loginLink.style.display = 'none';
      memberMenu.style.display = 'block';

      const memberName = status.memberName || '會員';
      memberAvatar.title = memberName;

      if (memberGreeting) {
        memberGreeting.textContent = `貴賓，${memberName}`;
      }
    } else {
      loginLink.style.display = 'inline-flex';
      memberMenu.style.display = 'none';
    }

    memberAvatar.addEventListener('click', function (event) {
      event.stopPropagation();
      memberDropdown.classList.toggle('show');
    });

    memberDropdown.addEventListener('click', function (event) {
      event.stopPropagation();
    });

    document.addEventListener('click', function () {
      memberDropdown.classList.remove('show');
    });

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        memberDropdown.classList.remove('show');
      }
    });

    logoutBtn.addEventListener('click', logoutMember);
  }

  document.addEventListener('DOMContentLoaded', function () {
    bindComingSoonLinks();
    bindNotifyButton();
    initHeaderMemberState();
  });
})();
