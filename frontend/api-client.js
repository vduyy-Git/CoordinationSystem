(function () {
  const API_BASE_KEY = 'coord.api.baseUrl';
  const TOKEN_KEY = 'coord.accessToken';
  const USER_KEY = 'coord.currentUser';

  function baseUrl() {
    const custom = localStorage.getItem(API_BASE_KEY);
    if (custom && custom.indexOf(':8080') !== -1) {
      localStorage.setItem(API_BASE_KEY, custom.replace(':8080', ':8088'));
    }
    return (localStorage.getItem(API_BASE_KEY) || 'http://localhost:8088/api').replace(/\/+$/, '');
  }

  async function request(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json');
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) headers.set('Authorization', `Bearer ${token}`);
    let response;
    try {
      response = await fetch(`${baseUrl()}${path.startsWith('/') ? path : `/${path}`}`, { ...options, headers });
    } catch (networkError) {
      throw new Error(`Không thể kết nối đến máy chủ Backend (${baseUrl()}). Vui lòng kiểm tra và chạy file start.bat!`);
    }
    const contentType = response.headers.get('content-type') || '';
    const payload = contentType.includes('application/json') ? await response.json() : await response.text();
    if (!response.ok || (payload && payload.success === false)) {
      const message = payload && payload.message ? payload.message : `API error (${response.status})`;
      if (response.status === 401) clearSession();
      throw new Error(message);
    }
    return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload;
  }

  function json(method, body) { return { method, body: JSON.stringify(body) }; }
  function setSession(auth) {
    const token = auth && (auth.token || auth.accessToken);
    if (!token) throw new Error('Máy chủ đăng nhập không trả về token.');
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(auth));
  }
  function clearSession() { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); }
  function user() {
    try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); }
    catch (_) { return null; }
  }
  function requireSession(redirectPath = '../index.html') {
    if (!localStorage.getItem(TOKEN_KEY)) {
      const next = encodeURIComponent(location.pathname + location.search);
      location.href = `${redirectPath}?next=${next}`;
      return false;
    }
    return true;
  }
  function error(error) { window.alert(error && error.message ? error.message : 'Không thể kết nối máy chủ.'); }

  window.CoordAPI = {
    request,
    get: path => request(path),
    post: (path, body) => request(path, json('POST', body)),
    put: (path, body) => request(path, json('PUT', body)),
    del: path => request(path, { method: 'DELETE' }),
    login: async (identifier, password) => {
      const auth = await request('/auth/login', json('POST', { identifier, password }));
      setSession(auth);
      return auth;
    },
    register: body => request('/auth/register', json('POST', body)),
    setSession, clearSession, user, requireSession, error,
    setBaseUrl: value => localStorage.setItem(API_BASE_KEY, value.trim().replace(/\/+$/, '')),
    baseUrl,
    async download(path, filename) {
      const headers = new Headers();
      const tokenValue = localStorage.getItem(TOKEN_KEY);
      if (tokenValue) headers.set('Authorization', `Bearer ${tokenValue}`);
      const response = await fetch(`${baseUrl()}${path.startsWith('/') ? path : `/${path}`}`, { headers });
      if (!response.ok) throw new Error('Không thể tải báo cáo.');
      const blob = await response.blob();
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = filename;
      link.click();
      URL.revokeObjectURL(link.href);
    }
  };
})();
