(function () {
  'use strict';

  const state = { page: 0, totalPages: 0, articleId: null };
  const el = (id) => document.getElementById(id);
  const motion = { lenis: null, cardContext: null, detailContext: null };

  function hasFullMotion() {
    return !window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }

  function initMotion() {
    if (!hasFullMotion() || !window.gsap) return;

    if (window.ScrollTrigger) {
      window.gsap.registerPlugin(window.ScrollTrigger);
    }

    window.gsap.timeline({ defaults: { ease: 'power2.out' } })
      .from('.article-hero p', { autoAlpha: 0, y: 12, duration: .55 })
      .from('.article-hero h1', { autoAlpha: 0, y: 22, duration: .75 }, '-=.3')
      .from('.article-hero .gold-line', { scaleX: 0, duration: .65 }, '-=.35');

    const smoothPointer = window.matchMedia('(pointer: fine)').matches;
    if (smoothPointer && window.Lenis) {
      motion.lenis = new window.Lenis({
        duration: 1.05,
        smoothWheel: true,
        syncTouch: false,
        wheelMultiplier: .9
      });
      if (window.ScrollTrigger) motion.lenis.on('scroll', window.ScrollTrigger.update);
      window.gsap.ticker.add((time) => motion.lenis.raf(time * 1000));
      window.gsap.ticker.lagSmoothing(0);
    }
  }

  function revealCards() {
    if (!hasFullMotion() || !window.gsap) return;
    if (motion.cardContext) motion.cardContext.revert();
    motion.cardContext = window.gsap.context(() => {
      window.gsap.from('.article-card', {
        autoAlpha: 0,
        y: 28,
        duration: .65,
        stagger: .08,
        ease: 'power2.out',
        clearProps: 'all',
        scrollTrigger: window.ScrollTrigger ? {
          trigger: '#articleGrid',
          start: 'top 84%',
          once: true
        } : undefined
      });
    }, el('articleGrid'));
  }

  function revealDetail() {
    if (!hasFullMotion() || !window.gsap) return;
    if (motion.detailContext) motion.detailContext.revert();
    motion.detailContext = window.gsap.context(() => {
      window.gsap.from('.detail-cover:not([hidden]), .detail-meta, #detailTitle, #detailContent', {
        autoAlpha: 0,
        y: 22,
        duration: .7,
        stagger: .1,
        ease: 'power2.out',
        clearProps: 'all'
      });

      if (window.ScrollTrigger) {
        window.gsap.to('#readingProgress', {
          scaleX: 1,
          ease: 'none',
          scrollTrigger: {
            trigger: '#detailView',
            start: 'top top',
            end: 'bottom bottom',
            scrub: .15
          }
        });
      }
    }, el('detailView'));
  }

  function formatDate(value) {
    if (!value) return '';
    return new Intl.DateTimeFormat('zh-TW', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value));
  }

  function card(article) {
    const node = document.createElement('article');
    node.className = 'article-card';
    const cover = document.createElement('div');
    cover.className = 'card-cover';
    if (article.coverImageUrl) {
      const image = document.createElement('img');
      image.src = article.coverImageUrl;
      image.alt = article.title + '封面';
      image.loading = 'lazy';
      cover.appendChild(image);
    }
    const body = document.createElement('div');
    body.className = 'card-body';
    const meta = document.createElement('div');
    meta.className = 'card-meta';
    meta.textContent = [article.category, formatDate(article.createAt), (article.viewCount || 0) + ' 次瀏覽'].join(' · ');
    const title = document.createElement('h3');
    title.textContent = article.title;
    const preview = document.createElement('p');
    preview.textContent = article.contentPreview || '';
    const link = document.createElement('a');
    link.href = '?article=' + article.articleId;
    link.className = 'read-more';
    link.textContent = '閱讀全文';
    body.append(meta, title, preview, link);
    node.append(cover, body);
    return node;
  }

  async function loadArticles(page) {
    el('articleMessage').textContent = '文章載入中...';
    el('articleGrid').replaceChildren();
    try {
      const response = await fetch('/thestar/content/articles?page=' + page + '&size=6');
      if (!response.ok) throw new Error();
      const data = await response.json();
      state.page = data.number;
      state.totalPages = data.totalPages;
      (data.content || []).forEach((article) => el('articleGrid').appendChild(card(article)));
      el('articleMessage').textContent = data.content && data.content.length ? '' : '目前還沒有已發布的文章。';
      el('pageInfo').textContent = state.totalPages ? (state.page + 1) + ' / ' + state.totalPages : '';
      el('prevPage').disabled = state.page <= 0;
      el('nextPage').disabled = state.page + 1 >= state.totalPages;
      revealCards();
      if (window.ScrollTrigger) window.ScrollTrigger.refresh();
    } catch (error) {
      el('articleMessage').textContent = '文章暫時無法載入，請稍後再試。';
    }
  }

  async function loadDetail(id) {
    el('listView').hidden = true;
    el('detailView').hidden = false;
    el('readingProgress').hidden = false;
    try {
      const response = await fetch('/thestar/content/articles/' + encodeURIComponent(id));
      if (!response.ok) throw new Error();
      const article = await response.json();
      state.articleId = article.articleId;
      document.title = article.title + '｜東方之星';
      el('detailCategory').textContent = article.category;
      el('detailDate').textContent = formatDate(article.createAt);
      el('detailViews').textContent = (article.viewCount || 0) + ' 次瀏覽';
      el('detailTitle').textContent = article.title;
      el('detailContent').textContent = article.content || '';
      if (article.coverImageUrl) {
        el('detailCover').hidden = false;
        el('detailCoverImage').src = article.coverImageUrl;
        el('detailCoverImage').alt = article.title + '封面';
      }
      revealDetail();
      if (window.ScrollTrigger) window.ScrollTrigger.refresh();
      await loadReviews(article.articleId);
      if (window.ScrollTrigger) window.ScrollTrigger.refresh();
    } catch (error) {
      el('detailTitle').textContent = '找不到這篇文章';
      el('detailContent').textContent = '文章可能已下架或不存在。';
      el('reviewForm').hidden = true;
    }
  }

  async function loadReviews(id) {
    const response = await fetch('/thestar/content/articles/' + id + '/reviews');
    if (!response.ok) return;
    const reviews = await response.json();
    el('reviewCount').textContent = reviews.length + ' 則';
    el('reviewList').replaceChildren();
    if (!reviews.length) {
      const empty = document.createElement('p');
      empty.className = 'state-message';
      empty.textContent = '還沒有留言，歡迎成為第一位分享者。';
      el('reviewList').appendChild(empty);
      return;
    }
    reviews.forEach((review) => {
      const item = document.createElement('article');
      item.className = 'review-item';
      const header = document.createElement('header');
      const member = document.createElement('span');
      member.textContent = '會員 #' + review.memberId;
      const date = document.createElement('time');
      date.textContent = formatDate(review.createdAt);
      const content = document.createElement('p');
      content.textContent = review.content;
      header.append(member, date);
      item.append(header, content);
      el('reviewList').appendChild(item);
    });
  }

  el('reviewForm').addEventListener('submit', async function (event) {
    event.preventDefault();
    el('reviewFormMessage').textContent = '送出中...';
    const content = el('reviewContent').value.trim();
    try {
      const response = await fetch('/thestar/content/articles/' + state.articleId + '/reviews', {
        method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: content })
      });
      if (response.status === 401) {
        el('reviewFormMessage').textContent = '請先登入會員再留言。';
        return;
      }
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || '留言送出失敗');
      }
      el('reviewContent').value = '';
      el('reviewFormMessage').textContent = '留言已送出';
      await loadReviews(state.articleId);
    } catch (error) {
      el('reviewFormMessage').textContent = error.message;
    }
  });

  async function changePage(page) {
    await loadArticles(page);
    const target = el('listView');
    if (motion.lenis) {
      motion.lenis.scrollTo(target, { offset: -24, duration: .8 });
    } else {
      target.scrollIntoView({ behavior: hasFullMotion() ? 'smooth' : 'auto' });
    }
  }

  el('prevPage').addEventListener('click', () => changePage(state.page - 1));
  el('nextPage').addEventListener('click', () => changePage(state.page + 1));
  el('backToList').addEventListener('click', () => { window.location.href = '/articles.html'; });

  const requestedId = new URLSearchParams(location.search).get('article');
  initMotion();
  if (requestedId && /^\d+$/.test(requestedId)) loadDetail(requestedId); else loadArticles(0);
}());
