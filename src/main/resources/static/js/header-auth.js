(function () {

  async function getMemberStatus() {
    try {
      const response = await fetch(
        "/api/member/status",
        {
          credentials: "same-origin"
        }
      );

      if (!response.ok) {
        return { loggedIn: false };
      }

      return await response.json();

    } catch (error) {
      return { loggedIn: false };
    }
  }

  async function logoutMember() {

    const currentPage =
      location.pathname
      + location.search
      + location.hash;

    let logoutTarget = currentPage;

    if (
      location.pathname === "/shop/cart"
      || location.pathname.startsWith("/shop/cart/")
    ) {
      logoutTarget = "/shop";

    } else if (
      location.pathname === "/profile.html"
      || location.pathname === "/coupons.html"
    ) {
      logoutTarget = "/index.html";

    } else if (
      location.pathname === "/roombooking.html"
      && new URLSearchParams(
        location.search
      ).get("tab") === "orders"
    ) {
      logoutTarget = "/roombooking.html";
    }

    try {
      await fetch(
        "/api/member/logout",
        {
          method: "POST",
          credentials: "same-origin"
        }
      );
    } catch (error) {
    }

    if (logoutTarget === currentPage) {
      location.reload();
    } else {
      location.href = logoutTarget;
    }
  }

  function bindComingSoonLinks() {

    document
      .querySelectorAll(".js-coming-soon")
      .forEach(function (link) {

        link.addEventListener(
          "click",
          function (event) {

            event.preventDefault();

            const message =
              link.dataset.message
              || "此功能尚未完成";

            alert(message);
          }
        );
      });
  }

  function formatNotificationTime(value) {

    if (!value) {
      return "";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat(
      "zh-TW",
      {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
      }
    ).format(date);
  }

  function createNotificationPanel(notifyBtn) {

    let panel =
      document.getElementById(
        "notificationPanel"
      );

    if (panel) {
      return panel;
    }

    panel = document.createElement("div");
    panel.id = "notificationPanel";
    panel.className = "notification-panel";

    panel.innerHTML = `
      <div class="notification-panel-header">
        <div>
          <strong>會員通知</strong>
          <span id="notificationUnreadText"></span>
        </div>

        <button
          type="button"
          id="markAllNotificationsRead">
          全部已讀
        </button>
      </div>

      <div
        id="notificationList"
        class="notification-list">
      </div>
    `;

    const navActions =
      notifyBtn.closest(".nav-actions");

    if (navActions) {
      navActions.appendChild(panel);
    } else {
      document.body.appendChild(panel);
    }

    panel.addEventListener(
      "click",
      function (event) {
        event.stopPropagation();
      }
    );

    return panel;
  }

  function updateNotificationDot(unreadCount) {

    const notifyDot =
      document.getElementById("notifyDot");

    if (!notifyDot) {
      return;
    }

    notifyDot.style.display =
      unreadCount > 0
        ? "block"
        : "none";
  }

  function renderNotifications(
    panel,
    data
  ) {

    const notificationList =
      panel.querySelector(
        "#notificationList"
      );

    const unreadText =
      panel.querySelector(
        "#notificationUnreadText"
      );

    const notifications =
      Array.isArray(data.notifications)
        ? data.notifications
        : [];

    const unreadCount =
      Number(data.unreadCount || 0);

    updateNotificationDot(unreadCount);

    unreadText.textContent =
      unreadCount > 0
        ? `未讀 ${unreadCount} 則`
        : "沒有未讀通知";

    notificationList.innerHTML = "";

    if (notifications.length === 0) {

      const empty =
        document.createElement("div");

      empty.className =
        "notification-empty";

      empty.textContent =
        "目前沒有通知";

      notificationList.appendChild(empty);

      return;
    }

    notifications.forEach(
      function (notification) {

        const item =
          document.createElement("button");

        item.type = "button";

        item.className =
          "notification-item";

        if (
          Number(notification.isRead) === 0
        ) {
          item.classList.add("unread");
        }

        const content =
          document.createElement("span");

        content.className =
          "notification-content";

        content.textContent =
          notification.content;

        const time =
          document.createElement("span");

        time.className =
          "notification-time";

        time.textContent =
          formatNotificationTime(
            notification.createdTime
          );

        item.appendChild(content);
        item.appendChild(time);

		item.addEventListener(
		  "click",
		  async function () {

		    const isUnread =
		      Number(notification.isRead) === 0;

		    /*
		     * 未讀通知先設為已讀。
		     */
		    if (isUnread) {
		      try {
		        await fetch(
		          `/api/member/notifications/${notification.memberNotifyId}/read`,
		          {
		            method: "POST",
		            credentials: "same-origin"
		          }
		        );
		      } catch (error) {
		        console.error(
		          "通知已讀更新失敗",
		          error
		        );
		      }
		    }

		    /*
		     * 目前優惠券相關通知，
		     * 點擊後前往我的優惠券頁面。
		     */
		    const content =
		      String(
		        notification.content || ""
		      );

		    if (
		      content.includes("優惠券")
		      || content.includes("新會員註冊禮")
		      || content.includes("生日券")
		    ) {
		      location.href =
		        "/coupons.html";

		      return;
		    }

			/*
			 * 訂單相關通知，點擊後前往我的購物訂單。
			 */
			if (
			  content.includes("購物訂單")
			  || content.includes("付款")
			) {
			  location.href = "/shop/order/myOrders";
			  return;
			}
			
		    /*
		     * 其他類型通知目前只更新已讀狀態。
		     */
		    await loadNotifications(panel);
		  }
		);

        notificationList.appendChild(item);
      }
    );
  }

  async function loadNotifications(panel) {

    const notificationList =
      panel.querySelector(
        "#notificationList"
      );

    notificationList.innerHTML =
      '<div class="notification-empty">通知讀取中</div>';

    try {

      const response = await fetch(
        "/api/member/notifications",
        {
          credentials: "same-origin",
          headers: {
            "Accept": "application/json"
          }
        }
      );

      if (!response.ok) {
        throw new Error(
          "通知讀取失敗"
        );
      }

      const data =
        await response.json();

      renderNotifications(
        panel,
        data
      );

    } catch (error) {

      notificationList.innerHTML =
        '<div class="notification-empty">通知讀取失敗</div>';
    }
  }

  function initNotificationFeature(
    loggedIn
  ) {

    const notifyBtn =
      document.getElementById("notifyBtn");

    if (!notifyBtn) {
      return;
    }

    if (!loggedIn) {

      updateNotificationDot(0);

      notifyBtn.addEventListener(
        "click",
        function () {

          const currentPage =
            location.pathname
            + location.search
            + location.hash;

          location.href =
            "/login.html?redirect="
            + encodeURIComponent(
              currentPage
            );
        }
      );

      return;
    }

    const panel =
      createNotificationPanel(
        notifyBtn
      );

    const markAllButton =
      panel.querySelector(
        "#markAllNotificationsRead"
      );

    notifyBtn.addEventListener(
      "click",
      async function (event) {

        event.stopPropagation();

        const memberDropdown =
          document.getElementById(
            "memberDropdown"
          );

        if (memberDropdown) {
          memberDropdown.classList.remove(
            "show"
          );
        }

        panel.classList.toggle("show");

        if (
          panel.classList.contains("show")
        ) {
          await loadNotifications(panel);
        }
      }
    );

    markAllButton.addEventListener(
      "click",
      async function () {

        await fetch(
          "/api/member/notifications/read-all",
          {
            method: "POST",
            credentials: "same-origin"
          }
        );

        await loadNotifications(panel);
      }
    );

    document.addEventListener(
      "click",
      function () {
        panel.classList.remove("show");
      }
    );

    loadNotifications(panel);
  }

  async function initHeaderMemberState() {

    const loginLink =
      document.getElementById("loginLink");

    const memberMenu =
      document.getElementById("memberMenu");

    const memberAvatar =
      document.getElementById("memberAvatar");

    const memberDropdown =
      document.getElementById(
        "memberDropdown"
      );

    const memberGreeting =
      document.getElementById(
        "memberGreeting"
      );

    const logoutBtn =
      document.getElementById("logoutBtn");

    if (
      !loginLink
      || !memberMenu
      || !memberAvatar
      || !memberDropdown
      || !logoutBtn
    ) {
      return;
    }

    const currentPage =
      location.pathname
      + location.search
      + location.hash;

    loginLink.href =
      "/login.html?redirect="
      + encodeURIComponent(
        currentPage || "/index.html"
      );

    const status =
      await getMemberStatus();

    if (status.loggedIn) {

      loginLink.style.display = "none";
      memberMenu.style.display = "block";

      const memberName =
        status.memberName || "會員";

      memberAvatar.title = memberName;

      if (memberGreeting) {
        memberGreeting.textContent =
          `貴賓，${memberName}`;
      }

    } else {

      loginLink.style.display =
        "inline-flex";

      memberMenu.style.display =
        "none";
    }

    initNotificationFeature(
      status.loggedIn
    );

    memberAvatar.addEventListener(
      "click",
      function (event) {

        event.stopPropagation();

        const notificationPanel =
          document.getElementById(
            "notificationPanel"
          );

        if (notificationPanel) {
          notificationPanel.classList.remove(
            "show"
          );
        }

        memberDropdown.classList.toggle(
          "show"
        );
      }
    );

    memberDropdown.addEventListener(
      "click",
      function (event) {
        event.stopPropagation();
      }
    );

    document.addEventListener(
      "click",
      function () {
        memberDropdown.classList.remove(
          "show"
        );
      }
    );

    document.addEventListener(
      "keydown",
      function (event) {

        if (event.key === "Escape") {

          memberDropdown.classList.remove(
            "show"
          );

          const notificationPanel =
            document.getElementById(
              "notificationPanel"
            );

          if (notificationPanel) {
            notificationPanel.classList.remove(
              "show"
            );
          }
        }
      }
    );

    logoutBtn.addEventListener(
      "click",
      logoutMember
    );
  }

  document.addEventListener(
    "DOMContentLoaded",
    function () {

      bindComingSoonLinks();
      initHeaderMemberState();
    }
  );

})();