(function () {
  var KEY = 'thestar-theme';

  function apply(theme) {
    document.documentElement.setAttribute('data-theme', theme);
  }

  function current() {
    return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
  }

  var saved = localStorage.getItem(KEY);
  apply(saved || (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'));

  window.toggleTheme = function () {
    var next = current() === 'dark' ? 'light' : 'dark';
    localStorage.setItem(KEY, next);
    apply(next);
  };
})();
